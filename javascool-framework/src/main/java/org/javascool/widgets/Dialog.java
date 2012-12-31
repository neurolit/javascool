package org.javascool.widgets;

import org.javascool.macros.Macros;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Définit un dialogue en popup qui gère l'aspect modal/non-modal.
 * <p>
 * Si le dialog est appelé de l'AWT il est non-modal (donc ne bloque pas l'AWT)
 * sinon il est modal (il blqieu jusqu'à complétion du dialogue).
 * <p>
 * Son utilisation typique se fait à travers une construction de la forme:
 * <p/>
 * <pre>
 * dialog = new NonModalDialog();
 * dialog.add(.. le composant du dialogue ..);
 * dialog.open(true);
 * </pre>
 * <p/>
 * </p>
 * <p>
 * Lorsque le composant du dialogue reçoit la réponse il ferme le dialogue, par
 * exemple:
 * <p/>
 * <pre>
 *  public void actionPerformed(ActionEvent e) {
 *    ... report de la valeur fournie par l'utilisateur ...
 *    messageDialog.close();
 * }
 * </pre>
 * <p/>
 * </p>
 */
public class Dialog extends JDialog {
    private static final long serialVersionUID = 1L;

    // @bean
    public Dialog() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pending = false;
            }
        });
    }

    /**
     * Ouvre le dialogue et entre en attente d'un retour de l'utilisateur.
     *
     * @param modal Si true le dialogue est bloquant et attend la réponse de
     *              l'utilisateur, si false il est non-modal et renvoie la main
     *              AVANT que le dialogue soit complété.
     *              <p>
     *              Il ne faut pas appelé le dialogue en mode modal directement
     *              d'un gestionnaire d'événement (bouton, etc..) mais utiliser un
     *              Thread.
     *              </p>
     */
    public void open(boolean modal) {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
        if (modal && SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                    "Impossible d'utiliser un dialogue modal directement d'un événement de l'interface: créer un thread");
        }
        pending = modal;
        while (pending) {
            Macros.sleep(100);
        }
    }

    /**
     * Routine à appeler quand le dialogue à été achevé pour continuer le
     * programme.
     */
    public void close() {
        dispose();
        pending = false;
    }

    /**
     * Teste si le dialogue est en cours ou achevé.
     * return La valeur true si le dialogue est en cours, sinon false.
     */
    public boolean isOpen() {
        return pending;
    }

    private boolean pending = false;
}
