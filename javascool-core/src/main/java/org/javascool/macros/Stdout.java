/*********************************************************************************
 * Philippe.Vienne@sophia.inria.fr, Copyright (C) 2011. All rights reserved. *
 * Guillaume.Matheron@sophia.inria.fr, Copyright (C) 2011. All rights reserved.
 * *
 * Thierry.Vieville@sophia.inria.fr, Copyright (C) 2009. All rights reserved. *
 *********************************************************************************/

package org.javascool.macros;

/**
 * Cette factory contient des fonctions générales rendues visibles à
 * l'utilisateur de proglets.
 * <p>
 * Elle permet de définir des fonctions statiques qui seront utilisées pour
 * faire des programmes élèves.
 * </p>
 * <p>
 * Elle permet aussi avoir quelques fonctions de base lors de la création de
 * nouvelles proglets.
 * </p>
 *
 * @serial exclude
 * @see <a href="Stdout.java.html">code source</a>
 */
public class Stdout {
    // @factory
    private Stdout() {
    }

    /**
     * Affiche dans la console une chaîne de caractères ou la représentation
     * textuelle d'un objet sur la console.
     * - Cette fonction ne change pas le focus de javascool.
     *
     * @param string La chaine ou l'objet à afficher sous sa représentation
     *               textuelle.
     * @see #println(String)
     */
    public static void echo(String string) {
        Stdout.println(string);
    }

    /**
     * @see #echo(String)
     */
    public static void echo(int string) {
        Stdout.echo("" + string);
    }

    /**
     * @see #echo(String)
     */
    public static void echo(char string) {
        Stdout.echo("" + string);
    }

    /**
     * @see #echo(String)
     */
    public static void echo(double string) {
        Stdout.echo("" + string);
    }

    /**
     * @see #echo(String)
     */
    public static void echo(boolean string) {
        Stdout.echo("" + string);
    }

    /**
     * @see #echo(String)
     */
    public static void echo(Object string) {
        Stdout.echo("" + string);
    }

    /**
     * Affiche dans la console une chaîne de caractères ou la représentation
     * textuelle d'un objet sur la console.
     * - Cette fonction ramène le focus de javascool sur la console.
     *
     * @param string La chaine ou l'objet à afficher sous sa représentation
     *               textuelle.
     * @see #echo(String)
     */
    public static void println(String string) {
        Pane.focusOnConsolePanel();
        System.out.print(string + "\n");
    }

    /**
     * @see #echo(String)
     */
    public static void println(int i) {
        Stdout.println("" + i);
    }

    /**
     * @see #echo(String)
     */
    public static void println(char i) {
        Stdout.println("" + i);
    }

    /**
     * @see #echo(String)
     */
    public static void println(double d) {
        Stdout.println("" + d);
    }

    /**
     * @see #echo(String)
     */
    public static void println(boolean b) {
        Stdout.println("" + b);
    }

    /**
     * @see #echo(String)
     */
    public static void println(Object o) {
        Stdout.println("" + o);
    }

    /**
     * Affiche dans la console une chaîne de caractères ou la représentation
     * textuelle d'un objet sur la console sans retour à la ligne.
     *
     * @param string La chaine ou l'objet à afficher sous sa représentation
     *               textuelle.
     */
    public static void print(String string) {
        Pane.focusOnConsolePanel();
        System.out.print(string);
        System.out.flush();
    }

    /**
     * @see #print(String)
     */
    public static void print(int i) {
        Stdout.print("" + i);
    }

    /**
     * @see #print(String)
     */
    public static void print(char i) {
        Stdout.print("" + i);
    }

    /**
     * @see #print(String)
     */
    public static void print(double d) {
        Stdout.print("" + d);
    }

    /**
     * @see #print(String)
     */
    public static void print(boolean b) {
        Stdout.print("" + b);
    }

    /**
     * @see #print(String)
     */
    public static void print(Object o) {
        Stdout.print("" + o);
    }

    /**
     * Efface tout ce qui est écrit dans la console.
     */
    public static void clear() {
        if (Pane.getConsoleListener() != null) {
            Pane.getConsoleListener().clear();
        }
    }

    /**
     * Sauve ce qui est présentement écrit dans la console dans un fichier.
     *
     * @param location La localisation (chemin du fichier ou localisation internet)
     *                 où sauver le texte.
     */
    public static void saveConsoleOutput(String location) {
        if (Pane.getConsoleListener() != null) {
            Pane.getConsoleListener().save(location);
        }
    }
}
