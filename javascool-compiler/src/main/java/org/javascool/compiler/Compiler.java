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
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Compilateur de Classes Java. Cette classe permet de compiler des fichiers Java d'un répertoire puis ensuite d'en
 * disposer dans la JVM par un ClassLoader prévu à cette effet. Ce compilateur est une sur-couche du Apache Commons JCI
 * Eclipse. Ce compilateur necessite pour fonctionner : <ul> <li>Un répertoire de sources</li> <li>Des fichiers à
 * compiler, cela peut être : <ul> <li>Un tableau de fichiers absolut ou relatifs étant dans les sources : {@link
 * #Compiler(java.io.File, java.io.File...)}</li> <li>Un nom de classe (ex. org.javascool.Main) : {@link
 * #Compiler(java.io.File, String)}</li> <li>Une recherche automatisé des fichiers dans le répertoire source : {@link
 * #Compiler(java.io.File)}</li> </ul> </li> </ul>
 *
 * @see org.eclipse.jdt.internal.compiler.tool.EclipseCompiler Librairie utilisé pour la compilation
 * @see org.apache.commons.io Librairie utilisé pour la recherche et manipulation de fichiers
 */
public class Compiler {

    /**
     * Liste des fichiers à compiler
     */
    private File[] filesToCompile;
    /**
     * Répertoire contenant les sources
     */
    private File srcDirectory;

    /**
     * Initialise le compilateur avec un répertoire et une liste de fichiers
     *
     * @param directory Le répertoire où on doit travailler
     * @param javas     Les fichiers à compiler
     */
    public Compiler(File directory, File... javas) {
        assertDirectoryExists(directory);
        srcDirectory = directory;
        ArrayList<File> sources = new ArrayList<File>(javas.length);
        for (File javaSource : javas) {
            if (!javaSource.isAbsolute()) javaSource = new File(directory, javaSource.getPath());
            assertFileExists(javaSource);
            sources.add(javaSource);
        }
        filesToCompile = FileUtils.convertFileCollectionToFileArray(sources);
    }

    /**
     * Initialise le compilateur avec un répertoire et un nom de classe.
     *
     * @param directory Le répertoire où on doit travailler
     * @param clazz     La classe à compiler
     */
    public Compiler(File directory, String clazz) {
        assertDirectoryExists(directory);
        srcDirectory = directory;
        assertFileExists(directory, decomposePathForAJavaSource(clazz));
        filesToCompile = new File[]{FileUtils.getFile(directory, decomposePathForAJavaSource(clazz))};
    }

    /**
     * Initialize un compilateur pour compiler tous les java d'un dossier.
     *
     * @param directory Le répertoire où on doit travailler
     */
    public Compiler(File directory) {
        assertDirectoryExists(directory);
        srcDirectory = directory;
        filesToCompile = FileUtils.convertFileCollectionToFileArray(
                FileUtils.listFiles(directory, new String[]{"java"}, true));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////   Fonctions d'assertion
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * S'assure que le dossier passé en argument existe bien et que c'est un dossier.
     *
     * @param directory Le dossier à tester
     * @throws IllegalArgumentException Si l'assertion n'est pas respecté.
     */
    private static void assertDirectoryExists(File directory) {
        if (!directory.exists())
            throw new IllegalArgumentException("Le dossier " + directory + " n'existe pas, on ne peut pas continuer");
        if (directory.isFile())
            throw new IllegalArgumentException(directory + " est un fichier et non un dossier, on ne peut pas continuer");
    }

    /**
     * S'assure que le fichier passé en argument existe bien et que c'est un fichier.
     *
     * @param file Le fichier à tester
     * @throws IllegalArgumentException Si l'assertion n'est pas respecté.
     */
    private static void assertFileExists(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("Le fichier " + file + " n'existe pas, on ne peut pas continuer");
        if (file.isDirectory())
            throw new IllegalArgumentException(file + " est un dossier et non un fichier, on ne peut pas continuer");
    }

    /**
     * S'assure que le fichier passé en argument existe bien et que c'est un fichier.
     *
     * @param file Le fichier à tester
     * @param path Le reste du chemin au fichier
     * @see #assertFileExists(java.io.File)
     */
    private static void assertFileExists(File file, String... path) {
        assertFileExists(FileUtils.getFile(file, path));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////   Utilitaires
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Décompose un nom de classe vers le chemin du fichier source. Permet de passer de "org.javascool.compiler.Main" à
     * ["org","javascool","compiler","Main.java"] qui une représentation du chemin du source sous la forme d'un tableau
     * de chaines.
     *
     * @param className Le nom de la classe au format Java
     * @return Le tableau représentant le chemin.
     */
    private static String[] decomposePathForAJavaSource(String className) {
        String[] path = className.split("\\.");
        path[path.length - 1] = path[path.length - 1] + ".java";
        return path;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////   Fonctions pour la compilation
    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Lance la compilation des fichiers. Va compiler les fichier dans le répertoire des sources. Les .class se
     * retrouverons donc avec les .java.
     *
     * @return Le résultat de la compilation
     * @see #compile(java.io.File)
     */
    public ArrayList<Diagnostic<? extends JavaFileObject>> compile() {
        return compile(srcDirectory);
    }

    /**
     * Lance la compilation des fichiers vers un autre répertoire. Va compiler les fichier dans un autre répertoire.
     *
     * @param binDirectory Répertoire cible pour la compilation. Il doit déjà exister et être un dossier
     * @return Les erreurs de la compilation
     */
    public ArrayList<Diagnostic<? extends JavaFileObject>> compile(File binDirectory) {
        assertDirectoryExists(binDirectory);
        final ArrayList<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<Diagnostic<? extends JavaFileObject>>();

        ArrayList<String> sourceFiles = new ArrayList<String>(filesToCompile.length);
        for (File srcFile : filesToCompile)
            sourceFiles.add(srcDirectory.toURI().relativize(srcFile.toURI()).getPath());
        classLoader = new JVSClassLoader(binDirectory);

        JavaCompiler eclipseCompiler;
        ArrayList<String> options;
        eclipseCompiler = new EclipseCompiler();
        options = getCompilerOptions();

        DiagnosticListener<? super JavaFileObject> diagnosticListener = new DiagnosticListener<JavaFileObject>() {
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                errors.add(diagnostic);
            }
        };

        StandardJavaFileManager fileManager =
                eclipseCompiler.getStandardFileManager(diagnosticListener, null, Charset.forName("UTF-8"));
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjects(sourceFiles.toArray(new String[sourceFiles.size()]));

        eclipseCompiler.getTask(null, fileManager, diagnosticListener, options, null, compilationUnits).call();

        return errors;
    }

    /**
     * Génère les options pour le compilateur Eclipse. <a href="http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_compile.htm">
     * Specs du Compilateur</a>
     *
     * @return Le tableau des options
     */
    private ArrayList<String> getCompilerOptions() {
        ArrayList<String> o = new ArrayList<String>();
        o.add("-nowarn");
        o.add("-1.6");
        o.add("-Xemacs");
        return o;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    /////   Fonctions pour obtenir un classloader sur les classes compilés
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private ClassLoader classLoader;

    /**
     * Permet d'obtenir un ClassLoader sur les classes compilés. Le ClassLoader est créé lors de la compilation. Il
     * permet d'acceder aux classes compilés même si elle n'ont pas été incluses dans le classpath d'origine.
     *
     * @return Un magnifique ClassLoader
     * @see JVSClassLoader
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null)
            throw new IllegalStateException("On ne peut pas créer un ClassLoader sur des sources non-compilés");
        return classLoader;
    }

}
