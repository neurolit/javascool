package org.javascool.manager.proglets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Classe gestionnaire des proglets.
 * 
 * <p>Cette classe permet de connaître les différentes 
 * proglets présentes dans le classpath actuel.</p>
 * 
 * <p>Cela va permettre par exemple de créer un tableau 
 * de démarrage avec les différentes proglets.</p>
 * 
 * <p>Une fois les proglets localisés, il utilise un 
 * {@link ArrayList} pour les classer.</p>
 * 
 * <p>Info : Dans un soucis de simplicité, les méthodes 
 * de cette classe sont statiques afin de pouvoir y 
 * acceder de façon simple.</p>
 * 
 * @author PhilippeGeek
 * @since 4.0
 * @version 5.0
 *
 */
public class Manager {
	
	/**
	 * Variable permettant de connaître les proglets présant sans rescanner tout le classpath.
	 * <p><em>Cette variable est volontairment privé car son accès doit se faire par des
	 * accesseurs public</em></p>
	 */
	private static final ArrayList<Proglet> proglets=new ArrayList<Proglet>();
	
	/**
	 * Constructeur muet.
	 * <p>Il est impossible d'instancier cette classe car ses méthodes sont uniquement statique</p>
	 */
	private Manager(){}
	
	/**
	 * Recherche des proglets.
	 * <p>Une Proglet est considérable comme un service, elle a dans sont package une classe qui
	 * implémente {@link Proglet} et qui est enregistré comme service dans les méta-informations
	 * du Jar.</p>
	 * <p>Cette méthode est nouvelle et doit absolument être mise à l'épreuve pour vérifier qu'elle
	 * ne peut pas être source de bugs</p>
	 * @since 5.0
	 * @return La listes de Proglets trouvés.
	 * @see ServiceLoader
	 */
	private static ArrayList<Proglet> findProglets(){
		final ArrayList<Proglet> progletsFound=new ArrayList<Proglet>();
		final ServiceLoader<Proglet> serviceLoader=ServiceLoader.load(Proglet.class);
		for(Proglet p:new Iterable<Proglet>() {
			public Iterator<Proglet> iterator() {
				return serviceLoader.iterator();
			}
		})
			progletsFound.add(p);
		return progletsFound;
	}
	
	/**
	 * Ajoute une proglet manuellement au gestionnaire.
	 * <p>Cette méthode permet d'ajouter un proglet qui pourrait ne pas avoir été chargé
	 * lors du scan du classpath. Ou alors une proglet créé à la volé lors de l'execution
	 * du code de l'application</p>
	 * @param proglet La proglet à ajouter.
	 * @throw IllegalArgumentException Dans le cas où la proglet est déjà présente
	 */
	public static void addProglet(final Proglet proglet) throws IllegalArgumentException{
		if(getProglets().contains(proglet))
			throw new IllegalArgumentException("La proglet "+proglet+" est déclaré deux fois");
		getProglets().add(proglet);
	}
	
	private static ArrayList<Proglet> getProglets() {
		return proglets;
	}

	static{ // Constructeur pour les données de la classe
		getProglets().addAll(findProglets()); // On aujoute les proglets du classpath
	}

}
