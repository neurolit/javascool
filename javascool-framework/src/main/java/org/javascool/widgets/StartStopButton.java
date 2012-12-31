package org.javascool.widgets;

import org.javascool.macros.Macros;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Définit un bouton de start/stop avec affichage du temps de calcul.
 *
 * @author Philippe Vienne
 * @serial exclude
 * @see <a href="StartStopButton.java.html">code source</a>
 */
public abstract class StartStopButton extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     * Le bouton de start/stop .
     */
    private JButton startButton;
    /**
     * L'affichage du temps d'exécution.
     */
    private JLabel execTime;
    /**
     * Thread du runnable.
     */
    private Thread runnableThread = null;
    /**
     * Thread du runnable.
     */
    private Thread timerThread = null;

    // @bean
    public StartStopButton() {
        setOpaque(false);
        add(startButton = new JButton("Arrêter"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning()) {
                    doStop();
                } else {
                    doStart();
                }
            }
        });
        add(execTime = new JLabel("  Temps d'exécution : 0 min 0 sec"));
        doStop();
    }

    /**
     * Lancement programmatique du programme et du compteur de temps.
     */
    public void doStart() {
        execTime.setText("  Temps d'exécution : 0 min 0 sec");
        startButton.setText("Arrêter");
        startButton.setIcon(Macros
                .getIcon("org/javascool/widgets/icons/stop.png"));
        if (isRunning()) {
            stop();
        }
        (runnableThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start();
                } catch (Throwable e) {
                    org.javascool.core.Jvs2Java.report(e);
                }
                doStop();
            }
        })).start();
        (timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (runningTime = 0; isRunning(); runningTime++) {
                        execTime.setText("  Temps d'exécution : " + runningTime / 60 + " min " + runningTime % 60 + " sec");
                        execTime.revalidate();
                        Macros.sleep(1000);
                    }
                } catch (Throwable e) {
                }
            }
        })).start();
        (timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int t = 0; isRunning(); t++) {
                        execTime.setText("  Temps d'exécution : " + t / 60
                                + " min " + t % 60 + " sec");
                        execTime.revalidate();
                        Macros.sleep(1000);
                    }
                } catch (Throwable e) {
                }
            }
        })).start();
    }

    /**
     * Arrêt programmatique du programme et du compteur de temps.
     */
    public void doStop() {
        if (isRunning()) {
            stop();
        }
        if (runnableThread != null) {
            runnableThread.interrupt();
            runnableThread = null;
        }
        if (timerThread != null) {
            timerThread.interrupt();
            timerThread = null;
        }
        startButton.setText("Exécuter");
        startButton.setIcon(Macros
                .getIcon("org/javascool/widgets/icons/play.png"));
    }

    /**
     * Renvoie l'état du processus, actif ou non.
     */
    public boolean isRunning() {
        return "Arrêter".equals(startButton.getText());
    }

    /**
     * Renvoie le temps d'éxcution écoulé en seconde.
     *
     * @return Le temps de la dernière exécution ou le temps d'exécution actuel.
     */
    public int getRunningTime() {
        return runningTime;
    }

    private int runningTime = 0;

    /**
     * Cette méthode est appelée au lancement demandé par l'utilisateur.
     * <p>Elle est exécutée dans un thread spécifique et arrêtée de force au stop.</p>
     */
    abstract public void start();

    /**
     * Cette méthode est appelée à l'arrêt demandé par l'utilisateur.
     * <p>
     * Elle est exécutée dans le thread courant et doit être non-bloquante.
     * </p>
     */
    public void stop() {
    }
}
