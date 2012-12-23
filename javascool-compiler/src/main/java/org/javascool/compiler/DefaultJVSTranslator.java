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

import java.io.File;
import java.io.IOException;

/**
 * Traducteur par défaut de code JVS vers du Java. Cette classe est une implémentation simpliste de la classe abstraite
 * JVSTranslator. Elle peut être utilisé pour traduire du JVS à la volé.
 */
public class DefaultJVSTranslator extends JVSTranslator {


    /**
     * Construit une instance du translator sur un Fichier.
     *
     * @param file Le fichier à traduire
     * @throws java.io.IOException En cas d'erreur lors de la lecture
     * @see JVSTranslator#JVSTranslator(java.io.File)
     */
    public DefaultJVSTranslator(File file) throws IOException {
        super(file);
    }

    /**
     * Traduit un code pseudo JVS pur vers un JVS. Le Translator Officiel ne fait aucune modification à cette endroit et
     * se charge juste de retourner le code tel qui lui a été fournit. Les modifications du JVS Officiel se trouve au
     * sein de la fonction #internalTranslate() qui implémente les specs du langage Java's Cool.
     *
     * @see JVSTranslator#internalTranslate()
     */
    @Override
    public String translate(String jvsCode) {
        return jvsCode;
    }
}
