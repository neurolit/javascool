/*
 * Java's Cool, IDE for French Computer Sciences Students
 * Copyright (C) 2012  Philippe VIENNE, INRIA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.javascool.compiler;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Permet de créer à partir d'un code source JVS, un code source Java. Pour personaliser le langage JVS, il suffit
 * d'étendre la classe et de sur-écrire la fonction #translate(String)
 *
 * @version 5.0
 */
public abstract class JVSTranslator {

    /**
     * Permet de disposer d'une numérotation incrémental durant l'éxecution de la JVM. Elle peut être utilisé par
     * exemple dans le cadre d'une numérotation des classe générés. Ce numéro est utilisé pour le serialVersionUID
     */
    protected static int INCREMENTAL_ID = 0;

    /**
     * Code Java à inclure avant le code traduit au sein de la nouvelle classe. Les %$COMPILATION_ID$% seront remplacé
     * par le {@link #INCREMENTAL_ID}.
     */
    protected static final String CODE_BEFORE =
            "" +
                    "  public static final long serialVersionUID = %$COMPILATION_ID$%L;" +
                    "  public void run() {" +
                    "   try{ main(); } catch(Throwable e) { " +
                    "    if (e.toString().matches(\".*Interrupted.*\"))" +
                    "      System.out.println" +
                    "         (\"\\n-------------------\\nProggramme arrêté !\\n-------------------\\n\");" +
                    "    else " +
                    "      System.out.println" +
                    "          (\"\\n-------------------\\nErreur lors de l'exécution de la proglet\\n\"+" +
                    "           (e)+\"\\n-------------------\\n\");" +
                    "   }" +
                    "  }";

    /**
     * Code Java à inclure après le code traduit au sein de la nouvelle classe. Les %$COMPILATION_ID$% seront remplacé
     * par le {@link #INCREMENTAL_ID}.
     */
    protected static final String CODE_AFTER = "";

    private String className = "JvsToJavaTranslated";

    {
        JVSTranslator.INCREMENTAL_ID++;
        className += JVSTranslator.INCREMENTAL_ID;
    }

    private String jvsCode;
    /**
     * Package de la classe compilé
     */
    private String packageName;
    /**
     * Classe parente de la classe compilé
     */
    private String parentClass = null;
    private File[] includeDirectories = new File[]{
            FileUtils.getUserDirectory(),
            FileUtils.getTempDirectory()
    };
    /**
     * L'extention par défaut pour un fichier à inclure. Cela peut être jvs par exemple mais peut aussi bien être xml.
     */
    protected static final String INCLUDE_FILE_EXTENTION = "jvs";
    /**
     * Classes implémentés par la classe compilé
     */
    private ArrayList<String> implementedClasses = new ArrayList<String>();
    /**
     * Les classes à importer
     */
    private ArrayList<Import> imports = new ArrayList<Import>();

    /**
     * Construit une instance du translator sur un Fichier.
     *
     * @param file Le fichier à traduire
     * @throws java.io.IOException En cas d'erreur lors de la lecture
     */
    public JVSTranslator(File file) throws java.io.IOException {
        this.setJvsCode(FileUtils.readFileToString(file));
        this.setClassName(file.getName().split("\\.", 2)[0]);
        this.setIncludeDirectories(new File[]{
                file.getParentFile(),
                FileUtils.getUserDirectory(),
                FileUtils.getTempDirectory()
        });
    }

    /**
     * Ajoute une classe à importer.
     *
     * @param name     Le nom de la classe à importer
     * @param isStatic Définit si l'import est statique.
     */
    public Import addImport(String name, boolean isStatic) {
        Import imp = new Import(name, isStatic);
        imports.add(imp);
        return imp;
    }

    /**
     * Ajoute un import non-statique
     *
     * @see #addImport(String, boolean)
     */
    public Import addImport(String name) {
        return addImport(name, false);
    }

    /**
     * Supprime un import.
     *
     * @param name Le nom de la classe à enlever des imports
     * @return L'import supprimé ou null si aucun import n'a été supprimé.
     */
    public Import removeImport(String name) {
        for (Import imp : imports) {
            if (imp.getClassname().equals(name)) {
                imports.remove(imp);
                return imp;
            }
        }
        return null;
    }

    /**
     * Ajoute les imports minimales pour Java's Cool
     */
    public void addDefaultImports() {
        addImport("java.lang.Math.*", true);
        //for (String clazz : new String[]{"Macros", "Stdin", "Stdout"})
        //    addImport("org.javascool.macros." + clazz + ".*", true);
        implementedClasses.add("Runnable");
    }

    /**
     * Retourne le code en Java compilable
     */
    public String getJavaCode() {
        /*
      Le code Java généré
     */
        String javaCode = encapsulateInWrapper(internalTranslate());
        return javaCode;
    }

    /**
     * @see #encapsulateInWrapper(String)
     */
    private String internalTranslate() {
        return internalTranslate(jvsCode);
    }

    /**
     * Traduit le code JVS en Java. Cette fonction traduit le fichier de code JVS en classe Java Anonyme. Les Specs JVS
     * sont définit dans la documentation du package org.javascool.compiler .
     *
     * @param code Le code pre-JVS à traduire
     * @return Le code de la classe Java.
     */
    private String internalTranslate(String code) {
        if (code == null)
            throw new IllegalArgumentException("Aucun code à traduire");
        code = translate(code);
        String text = code.replace((char) 160, ' ');
        // On analyse rapidement le code de l'utilsateur pour trouver les éventuelles import, package ou include
        imports.addAll(Import.parseJavaImportDeclaration(text)); // On ajout les imports écrits dans le JVS
        StringBuilder javaCode = new StringBuilder();// Cette variable contient le code Java qui sera inclut dans la
        // classe entre les deux wrappers
        for (String line : text.split("\n")) { // On navigue dans le code ligne par ligne
            if (line.matches("^\\s*(import|package)[^;]*;\\s*$")) { // Ligne définisant un package ou un import
                if (line.matches("^\\s*package[^;]*;\\s*$")) { // Si c'est un package
                    packageName = line.replaceAll("^\\s*package([^;]*);\\s*$", "$1").trim();
                }
                javaCode.append("// ").append(line).append("\n");
            } else if (line.matches("^\\s*include[^;]*;\\s*$")) { // Inclusion d'un autre JVS
                String name = line.replaceAll("^\\s*include([^;]*);\\s*$", "$1").trim();
                javaCode.append("/* include ").append(name).append("; */ ");
                try {
                    javaCode.append(convertToOneLineCode(
                            internalTranslate(FileUtils.readFileToString(searchInclude(name)))
                    ));
                } catch (Exception e) {
                    javaCode.append("// Erreur : ").append(e.getMessage());
                }
                javaCode.append("\n");
            } else {
                javaCode.append(line).append("\n");
            }
        }
        return javaCode.toString();
    }

    /**
     * Recherche un fichier à inclure.
     *
     * @param include La valeur de l'include
     * @return Le pointeur vers le fichier
     * @throws FileNotFoundException Dans le cas où aucun fichier n'as pu être trouvé pour l'include
     */
    private File searchInclude(String include) throws FileNotFoundException {
        if (includeDirectories != null) {
            for (File directory : includeDirectories) {
                if (FileUtils.getFile(directory, include).exists())
                    return FileUtils.getFile(directory, include);
                if (FileUtils.getFile(directory, include + "." + INCLUDE_FILE_EXTENTION).exists())
                    return FileUtils.getFile(directory, include + "." + INCLUDE_FILE_EXTENTION);
            }
        }
        if (new File(include).exists())
            return new File(include);
        if (new File(include + "." + INCLUDE_FILE_EXTENTION).exists())
            return new File(include + "." + INCLUDE_FILE_EXTENTION);
        throw new FileNotFoundException("Impossible de trouver un fichier pour l'include : " + include);
    }

    /**
     * Convertie un code sur plusieurs ligne en code sur une seul ligne.
     *
     * @param code Le code à convertir
     * @return Le code convertie en une ligne
     */
    protected String convertToOneLineCode(String code) {
        StringBuilder builder = new StringBuilder();
        for (String line : code.split("\n")) {
            if (!line.contains("//"))
                builder.append(line);
        }
        return builder.toString();
    }

    /**
     * Crée l'encapsulation dans une classe en Java. Dans cette fonction, le Translator va écrire le necessaire pour
     * faire une classe Java. C'est à dire, le package s'il est définit, ensuite les imports, et enfin écrit la classe
     * en y mettant le code passée en argument.
     * <p/>
     * <note>Cette fonction peut être réécrite dans le cas où cette encapsulation n'est pas souhaitée.</note>
     *
     * @param code Le code à encapsuler dans la classe
     * @return Le code Java prêt à être compilé
     */
    protected String encapsulateInWrapper(String code) {
        StringBuilder javaCode = new StringBuilder();
        if (packageName != null)
            javaCode.append("package ").append(packageName).append(";");
        for (Import imp : imports) {
            javaCode.append(imp.getImportCode());
        }
        javaCode.append("public class ").append(className).append(" ");
        if (parentClass != null)
            javaCode.append("extends ").append(parentClass).append(" ");
        if (!implementedClasses.isEmpty()) {
            javaCode.append("implements ");
            for (String inter : implementedClasses) {
                if (!inter.equals(implementedClasses.get(0)))
                    javaCode.append(", ");
                javaCode.append(inter);
            }
            javaCode.append(" ");
        }
        String codeBefore = CODE_BEFORE.replace("%$COMPILATION_ID$%", "" + INCREMENTAL_ID),
                codeAfter = CODE_AFTER.replace("%$COMPILATION_ID$%", "" + INCREMENTAL_ID);
        javaCode.append("{").append(codeBefore).append(code).append(codeAfter).append("}");
        javaCode.append("// Compiled With Java's Cool");
        return javaCode.toString();
    }

    /**
     * Permet d'établir le nom de la classe compilé avec son package.
     *
     * @return Le nom au format java (ex. org.javascool.Main)
     */
    public String getFullClassname() {
        return ((packageName == null) || packageName.isEmpty()) ? className : (packageName + "." + className);
    }

    /**
     * Traduit un code JVS pur vers un pseudo Java. Cette fonction permet d'écrire des structures non-java pour les
     * proglets qui le souhaitent. Cela peut aller de l'annotation à remplacer par une fonction à une nouvelle syntaxe à
     * implémenter.
     *
     * @see DefaultJVSTranslator#translate(String)
     */
    public abstract String translate(String jvsCode);

    /**
     * Le nom de la classe Java qui sera généré
     */
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Le code d'origine en JVS qu'il faut transformer.
     */
    public String getJvsCode() {
        return jvsCode;
    }

    public void setJvsCode(String jvsCode) {
        this.jvsCode = jvsCode;
    }

    /**
     * Tableau des lieux pour rechercher les fichiers à inclure
     */
    public File[] getIncludeDirectories() {
        return includeDirectories;
    }

    public void setIncludeDirectories(File[] includeDirectories) {
        this.includeDirectories = includeDirectories;
    }

    /**
     * Cette classe permet de représenter un import à effectuer dans le code
     */
    public static class Import {

        /**
         * Analyse le code pour détecter des instructions d'import
         *
         * @param javaCode Le code à analyser
         * @return Les imports présents dans le code
         */
        public static ArrayList<Import> parseJavaImportDeclaration(String javaCode) {
            ArrayList<Import> list = new ArrayList<Import>();
            String[] instructions = javaCode.split("\n");
            for (String instruction : instructions) {
                if (instruction.contains("import ")) {
                    instruction = instruction.split(";", 2)[0].split("import ", 2)[1];
                    String[] parts = instruction.split(" ", 2);
                    try {
                        list.add(new Import(parts[0].equals("static") ? parts[1] : parts[0], parts[0].equals("static")));
                    } catch (Exception e) {
                        System.err.println("Impossible de lire l'instruction : " + instruction);
                    }
                }
            }
            return list;
        }

        private String classname;
        private boolean statik;

        public Import(String classname) {
            this(classname, false);
        }

        public Import(String classname, Boolean isStatic) {
            setClassname(classname);
            setStatic(isStatic);
        }

        /**
         * Le nom de la classe à importer. Cela peut être org.javascool.Utils dans le cas où on souhaite que le code
         * pour y acceder soit Utils.[fnc] mais il peut très bien être variable org.javascool.* .
         */
        public String getClassname() {
            return classname;
        }

        public void setClassname(String classname) {
            this.classname = classname;
        }

        /**
         * Définit si l'importation à faire est statique ou pas.
         */
        public boolean isStatic() {
            return statik;
        }

        public void setStatic(boolean statik) {
            this.statik = statik;
        }

        /**
         * Donne le code pour importer la classe dans un fichier Java
         *
         * @return Le code
         */
        public String getImportCode() {
            return "import " + (isStatic() ? "static " : "") + getClassname() + ";";
        }
    }

}
