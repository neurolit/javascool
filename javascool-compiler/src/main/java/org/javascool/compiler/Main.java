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

import org.apache.commons.cli.*;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Main class to run JVSC. Classe principale pour lancer le JVSC (Java's Cool Compiler). Son usage est le suivant :
 * <ul><li>java -jar jvsc.jar MyFile.jvs Cela va créer un MyFile.class dans le répertoire courant</li></ul> Pour avoir
 * un usage, executez la commande  'java -jar jvsc.jar -h'
 */
public class Main {

    /**
     * Variable stockant les arguments de l'application.
     */
    private static CommandLine commandLine;
    /**
     * Les options de l'application représentés dans un objet.
     */
    private static Options options;

    /**
     * Fonction pricipale du programme.
     *
     * @param args Les arguments de l'application.
     */
    public static void main(String... args) {
        try {
            parseArguments(args);
            if (getCommandLine().hasOption('h') || (!getCommandLine().hasOption("src") || getCommandLine().getOptionValue("src").isEmpty())) {
                if (!getCommandLine().hasOption("src") || getCommandLine().getOptionValue("src").isEmpty())
                    System.err.println("Aucun fichier source à compiler");
                printHelpMessage();
                return;
            }

            File jvsFile = new File("C:\\Users\\PhilippeGeek\\IdeaProjects\\Javascool-Compiler\\test.jvs");
            ProgletCodeCompiler codeCompiler = new ProgletCodeCompiler(ProgletCodeCompiler.DEFAULT_PROGLET, jvsFile);
            ArrayList<Diagnostic<? extends JavaFileObject>> result = codeCompiler.compile();
            for (Diagnostic<? extends JavaFileObject> error : result) {
                System.out.println("Erreur : " + error.toString());
            }
            if (result.size() == 0)
                codeCompiler.getCompiledRunnable().run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Affiche un message d'aide à l'écran. Il permet de préciser à l'utilisateur les option utilisables dans
     * l'application.
     *
     * @see #getOptions()
     */
    private static void printHelpMessage() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setLeftPadding(2);
        helpFormatter.printHelp("java -jar jvsc.jar", "Compilateur Java's Cool\nDétail des options :", getOptions(), "", true);
    }

    /**
     * Lit la ligne de commande. A partir des options de l'application, un "Parser" va décoder les lignes pour établir
     * les options. Le résultat de la fonction est socké dans une variable static
     *
     * @param args Les arguments de l'application
     * @throws ParseException En cas d'erreur lors de la lecture des arguments
     */
    private static void parseArguments(String... args) throws ParseException {
        BasicParser parser = new BasicParser();
        commandLine = parser.parse(getOptions(), args);
    }

    /**
     * Définit les options de l'application. A partir des outils Apache CLI, on définit une liste d'options en précisant
     * le préfixe, s'il y a un argument et enfin la description. Utiliser ce système permet ensuite d'affiche un usage
     * de façon relativement simple.
     *
     * @return L'objet représentant les options de l'application.
     */
    public static Options getOptions() {
        if (options == null) {
            options = new Options();
            options.addOption("o", true, "Le nom du fichier de sortie et accessoirement le nom de la classe java");
            options.addOption("src", "source", true, "Le fichier Java's Cool à compiler");
            options.addOption("p", "proglet", true, "La proglet à utiliser dans le classpath");
            options.addOption("f", false, "Force la compilation même en cas d'erreur");
            options.addOption("v", "verbose", false, "Affiche tous les message de Logging");
            options.addOption("h", "help", false, "Affiche l'aide");
        }
        return options;
    }

    /**
     * Permet d'obtenir la valeur des arguments actuelles de l'application.
     *
     * @return L'objet représentant les arguments.
     */
    public static CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Configure la valeurs de la ligne de commande. Attention, utiliser cette methode de manière non-approprié peut
     * avoir d'étranges effets sur l'execution du programme
     *
     * @param commandLine La valeur à mettre
     */
    public static void setCommandLine(CommandLine commandLine) {
        Main.commandLine = commandLine;
    }
}
