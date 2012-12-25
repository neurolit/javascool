package org.javascool.core;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Cette classe modélise un fichier de Java's Cool.
 * <p>Tout en conservant les fonctionnalité de la classe {@link File},
 * cette classe permet d'y ajouter des fonctionnalité intéressante tel
 * que la liaison à une proglet, la compilation, l'exécution ...</p>
 * <p>Cette classe à pour le rôle de retenir des éléments sur un
 * fichier (A-t-il été modifié ? compilé ? Doit-il être sauvegarder ?
 * ...). Mais cette classe ne doit jamais appeler un élément graphique.
 * Cette interaction est géré depuis le javascool-ui.</p>
 * <p>Pour utiliser cette classe, on peut instancier de la même fâçon
 * qu'un fichier ou alors à partir d'un existant.<br>
 * {@code JVSFile file=new JVSFile((File)file);}<br>
 * {@code JVSFile file=new JVSFile((String)path);}</p>
 *
 * @author PhilippeGeek
 * @version 5.0
 */
public class JVSFile extends File {

    /**
     * L'encodage par défaut des fichiers Java's Cool.
     */
    private static final Charset ENCODING = Charsets.UTF_8;

    /**
     * Le serialVersion, à quoi sert-il ? on se le demande !
     */
    private static final long serialVersionUID = -8992334089819709141L;

    /**
     * Contenu du fichier sur le disque dur.
     */
    private String content;

    /**
     * Contenu du fichier dans le logiciel.
     */
    private String contentNotSaved;

    /**
     * Crée un instance à partir d'un répertoir parent et d'un enfant supposé.
     *
     * @param parent Le répertoire parent
     * @param child  l'enfant dans ce répertoire.
     */
    public JVSFile(File parent, String child) {
        super(parent, child);
        loadFromFile();
    }

    /**
     * Crée un instance à partir d'une adresse.
     *
     * @param path L'adresse du fichier dans le système.
     * @see File#File(String)
     */
    public JVSFile(String path) {
        super(path);
        loadFromFile();
    }

    /**
     * Crée une instance à partir d'une {@link URI}.
     * <p>Cette dernière doit absolument avoir un protocole file.</p>
     * <p>Pour ouvrir une ressource sur le WEB comme un fichier web,
     * il est préférable d'utiliser la méthode statique
     * {@link #loadFromWeb(URI uri)}</p>
     *
     * @param uri L'URI à ouvrir.
     * @see File#File(URI)
     */
    public JVSFile(URI uri) {
        super(uri);
        loadFromFile();
    }

    /**
     * Lit le fichier à partir du disque dur.
     * <p>En cas d'erreur, le contenu du fichier sera estimé comme vide.
     * On loggue tout de même l'erreur mais elle ne doit pas troubler
     * le fonctionnement de l'application.</p>
     * <p>Par la suite, la fonction {@link #isLoaded()} permet de savoir
     * si le fichier a correctement été chargé.</p>
     */
    private void loadFromFile() {
        try {
            content = contentNotSaved = FileUtils.readFileToString(this, JVSFile.ENCODING);
        } catch (IOException e) {
            LogFactory.getLog(JVSFile.class).error("Impossible d'ouvrir le fichier " + this, e);
        }
    }

    /**
     * Permet de savoir si le fichier a pu être chargé correctement.
     * <p>Si ce n'est pas le cas alors une erreur s'est surrement produite,
     * le fichier peut ne pas être accessible</p>
     *
     * @return vrai si le fichier est correctement chargé.
     */
    public boolean isLoaded() {
        return content != null;
    }

    /**
     * Met à jour le contenu temporaire du fichier.
     * <p>Ce contenu correspond à la valeur actuel du fichier dans le programme.
     * Par exemple ce qu'il y a écrit dans l'éditeur.</p>
     * <p>Il permet à la fonction {@link #hasToSave()} de fonctionner correctement</p>
     *
     * @param content Le contenu actuel du fichier
     */
    public void updateContent(String content) {
        contentNotSaved = content;
    }

    /**
     * Permet d'obtenir le contenu du fichier.
     * <p>Cette fonction fournit le contenu le plus récent disponible pour ce
     * fichier, cela peut donc être soit le contenu sur le disque, soit le
     * contenu à enregistrer.</p>
     *
     * @return Le contenu du fichier sous forme d'une chaîne de caractères
     */
    public String getContent() {
        return content;
    }

    /**
     * Met directement à jour le contenu du fichier.
     * <p>Lors de l'utilisation de cette fonction, la classe va enregistrer le
     * contenu passé en paramètre comme celui du fichier et va <b>l'écrire</b>
     * sur le disque dur.</p>
     *
     * @param content Le contenu à enregistrer
     * @return vrai si l'enregistrement s'est bien déroulé.
     */
    public boolean setContent(final String content) {
        updateContent(content);
        return save();
    }

    /**
     * Enregistre le contenu du fichier sur le disque dur.
     *
     * @return vrai si tout s'est bien passé.
     */
    public boolean save() {
        try {
            this.content = this.contentNotSaved;
            FileUtils.writeStringToFile(this, content, ENCODING);
            loadFromFile();
            return true;
        } catch (Exception e) {
            LogFactory.getLog(getClass()).error("Erreur lors de la sauvgarde de " + this, e);
            return false;
        }
    }

    /**
     * Permet de savoir si le fichier doit être sauvegardé.
     *
     * @return vrai si le fichier doit être sauvegardé
     */
    public boolean hasToSave() {
        return contentNotSaved.equals(content);
    }

    /**
     * Compare deux fichier pour savoir s'ils sont identique.
     * <p>Deux fichier sont estimés identiques si :
     * <ul><li>Ils ont le même chemain</li>
     * <li>Ils ont le même contenu sur le disque</li>
     * <li>Ils ont le même contenu en mémoire</li></ul></p>
     * <p><em>NB : Un objet File et JVSFile ne sont pas identiques</em></p>
     *
     * @return vrai si les deux fichiers sont complétement identiques.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JVSFile) {
            return getAbsolutePath().equals(((JVSFile) obj).getAbsolutePath()) &&
                    content.equals(((JVSFile) obj).content) &&
                    contentNotSaved.equals(((JVSFile) obj).contentNotSaved);
        } else {
            return false;
        }
    }

    /**
     * Représente sous forme d'une chaine le fichier.
     */
    @Override
    public String toString() {
        return getAbsolutePath();
    }
}
