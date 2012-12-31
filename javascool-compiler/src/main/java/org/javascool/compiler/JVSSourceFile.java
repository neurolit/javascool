package org.javascool.compiler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
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
 * {@code JVSSourceFile file=new JVSSourceFile((File)file);}<br>
 * {@code JVSSourceFile file=new JVSSourceFile((String)path);}</p>
 *
 * @author PhilippeGeek
 * @version 5.0
 */
public class JVSSourceFile extends File {

    /**
     * L'encodage par défaut des fichiers Java's Cool.
     */
    private static final Charset ENCODING = Charsets.UTF_8;

    /**
     * Le serialVersion, à quoi sert-il ? on se le demande !
     */
    private static final long serialVersionUID = -8992334089819709141L;

    /**
     * Permet de savoir si le fichier est temporaire ou non.
     */
    private boolean isTMP = false;

    /**
     * Contenu du fichier sur le disque dur.
     */
    private final StringProperty content = new SimpleStringProperty(this, "content", "");

    /**
     * Permet de retenir si le fichier doit être sauvegarder
     */
    private final BooleanProperty saved = new SimpleBooleanProperty(this, "saved", true);

    {
        content.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if (!s.equals(s2))
                    saved.setValue(false);
            }
        });
    }

    /**
     * Crée un fichier temporaire.
     *
     * @throws IOException Dans le cas où il est impossible de créer le fichier temporaire
     */
    public JVSSourceFile() throws IOException {
        super(File.createTempFile("jvs", ".jvs").getAbsolutePath());
        isTMP = true;
    }

    /**
     * Crée un instance à partir d'un répertoir parent et d'un enfant supposé.
     *
     * @param parent Le répertoire parent
     * @param child  l'enfant dans ce répertoire.
     */
    public JVSSourceFile(File parent, String child) throws IOException {
        super(parent, child);
        loadFromFile();
    }

    /**
     * Crée un instance à partir d'une adresse.
     *
     * @param path L'adresse du fichier dans le système.
     * @see File#File(String)
     */
    public JVSSourceFile(String path) throws IOException {
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
    public JVSSourceFile(URI uri) throws IOException {
        super(uri);
        loadFromFile();
    }

    /**
     * Lit le fichier à partir du disque dur.
     * <p>En cas d'erreur, le contenu du fichier sera estimé comme vide.
     * On loggue tout de même l'erreur mais elle ne doit pas troubler
     * le fonctionnement de l'application.</p>
     *
     * @throws IOException En cas d'erreur lors de l'ouverture du fichier.
     */
    private void loadFromFile() throws IOException {
        try {
            content.setValue(FileUtils.readFileToString(this, JVSSourceFile.ENCODING));
        } catch (IOException e) {
            LogFactory.getLog(JVSSourceFile.class).error("Impossible d'ouvrir le fichier " + this, e);
            throw e;
        }
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
        this.content.setValue(content);
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
        return this.content.getValue();
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
     * <p><em>NB : Un fichier temporaire ne peut pas être enregistrer. Si tel est le cas pour le fichier actuel,
     * alors la fonction renvera toujours faux.</em></p>
     *
     * @return vrai si tout s'est bien passé.
     */
    public boolean save() {
        if (isTempFile()) return false;
        try {
            FileUtils.writeStringToFile(this, content.getValue(), ENCODING);
            loadFromFile();
            saved.setValue(true);
            return true;
        } catch (Exception e) {
            LogFactory.getLog(getClass()).error("Erreur lors de la sauvgarde de " + this, e);
            saved.setValue(false);
            return false;
        }
    }

    /**
     * Permet de savoir si le fichier est temporaire.
     * <p>Lors de l'enregistrement d'un fichier temporaire, on doit demander à l'utilisateur où il souhaite
     * l'enregistrer.</p>
     *
     * @return vrai si le fichier est temporaire
     */
    public boolean isTempFile() {
        return isTMP;
    }

    /**
     * Permet de savoir si le fichier doit être sauvegardé.
     *
     * @return vrai si le fichier doit être sauvegardé
     */
    public boolean hasToSave() {
        return isTempFile() || saved.get();
    }

    /**
     * Compare deux fichier pour savoir s'ils sont identique.
     * <p>Deux fichier sont estimés identiques si :
     * <ul><li>Ils ont le même chemain</li>
     * <li>Ils ont le même contenu sur le disque</li>
     * <li>Ils ont le même contenu en mémoire</li></ul></p>
     * <p><em>NB : Un objet File et JVSSourceFile ne sont pas identiques</em></p>
     *
     * @return vrai si les deux fichiers sont complétement identiques.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JVSSourceFile) {
            return getAbsolutePath().equals(((JVSSourceFile) obj).getAbsolutePath()) &&
                    content.getValue().equals(((JVSSourceFile) obj).content.getValue()) &&
                    saved.getValue().equals(((JVSSourceFile) obj).saved.getValue());
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

    /**
     * Charge un fichier depuis le web dans un fichier temporaire.
     */
    public static JVSSourceFile loadFromWeb(URI fileUri) throws IOException {
        if (fileUri.getScheme().equals("file")) return new JVSSourceFile(fileUri);
        try {
            final JVSSourceFile file = new JVSSourceFile();
            IOUtils.copy(fileUri.toURL().openStream(), new FileOutputStream(file));
            file.loadFromFile();
            file.saved.set(false);
            return file;
        } catch (IOException e) {
            LogFactory.getLog(JVSSourceFile.class).error("Impossible d'ouvrir " + fileUri, e);
            throw e;
        }
    }
}
