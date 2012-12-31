package org.javascool.core;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class Proglet {

    /**
     * Liste les proglets du ClassPath actuel.
     * @return
     */
    public final static ArrayList<Proglet> getProglets(){
        final ArrayList<Proglet> proglets=new ArrayList<Proglet>();
        final ServiceLoader<Proglet> loader=ServiceLoader.load(Proglet.class);
        final Iterator<Proglet> iter=loader.iterator();
        while (iter.hasNext())
            proglets.add(iter.next());
        return proglets;

    }

    /**
     * Permet d'obtenir la classe du panneau graphique de la Proglet.
     * <p>Le retour peut être nul dans le cas où ce dernier n'existe pas.</p>
     * @return La classe du Panel.
     */
    public abstract Class<? extends JPanel> getPanelClass();

    /**
     * Permet d'obtenir la classe des Fonctions de la proglet.
     * <p>Le retour peut être nul dans le cas où ce dernier n'existe pas.</p>
     * @return La classe des Fonctions.
     */
    public abstract Class<?> getFunctionsClass();

    /**
     * Permet d'obtenir la classe du Translator de la proglet.
     * <p>Le retour peut être nul dans le cas où ce dernier n'existe pas.</p>
     * @return La classe des Fonctions.
     */
    public abstract Class<? extends Translator> getTranslatorClass();

    /**
     * Donne le package de déclaration de la proglet
     */
    public abstract String getProgletPackage();

}
