/**
 * 
 */
package org.javascool.manager.proglets;

import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * Représentation sous forme d'objet d'une proglet.
 * <p>Permet de géré et d'interagir avec les différentes fonctionnalités de la proglet :
 * 	<ul>
 * 		<li>Obtenir le compilateur correspondant à cette proglet</li>
 * 		<li>Le lien vers la documentation de la proglet</li>
 * 		<li>Les paramètres de la proglets tels que l'icône</li>
 * 	</ul>
 * </p>
 * <p>Cette classe est implémenter par chaque Proglet pour pouvoir s'identifier
 * comme service auprès de la JVM.</p>
 * <p>L'implémentation de cette classe doit normallement se faire automatiquement
 * par le ProgletBuilder qui est un plugin Maven</p>
 * <p>Pour plus d'information à ce sujet, consultez le site développeurs de Java's
 * Cool dans les spécifications sur les proglets.</p>
 * 
 * @author PhilippeGeek
 * @since 4.0
 * @version 5.0
 *
 */
public class Proglet {
	
	/**
	 * Le classLoader de la classe représentant les Proglets.
	 * <p>Cette variable permet d'acceder de façon plus explicite au ClassLoader sans
	 * avoir à écrire de longues lignes.</p>
	 */
	final static private ClassLoader CLASS_LOADER=Proglet.class.getClassLoader();
	
	/**
	 * Le classLoader de la classe représentant les Proglets.
	 * <p>Cette variable permet d'acceder de façon plus explicite au ClassLoader sans
	 * avoir à écrire de longues lignes.</p>
	 */
	final static private Log LOG=LogFactory.getLog(Proglet.class);

	/**
	 * Le package de la proglet.
	 */
	private String packageName;
	/**
	 * Le nom de la proglet.
	 * Ce nom est un nom lisible par les humains.
	 */
	private String progletName;
	/**
	 * L'identificateur de la proglet.
	 * Il est unique et correspond au dernier élément du package.
	 */
	private String identifier;
	/**
	 * La configuration de la proglet.
	 * Cela correspond au contenu du proglet.json.
	 */
	private JSONObject configuration;
	
	/**
	 * Initialise une proglet à partir de ses données.
	 * <p>Lorsque cette classe est implémenter, les variables tels que le package
	 * sont à écrire en dur en temps que Final. A ce moment, il est possible
	 * d'utiliser ce constructeur pour la proglet</p>
	 * 
	 * @throws ClassNotFoundException Si la proglet n'existe pas (Ici c'est rare mais possible)
	 * @throws IllegalStateException Dans le cas où ce n'est pas une proglet final
	 */
	public Proglet() throws IllegalStateException, ClassNotFoundException{
		if(packageName==null||identifier==null)
			throw new IllegalStateException("La proglet n'est pas configuré", new NullPointerException("Le package ou l'identificateur est null"));
		configuration=loadConfiguration();
		if(configuration==null)
			throw new ClassNotFoundException("La proglet "+this+" n'est pas présente");
	}
	
	/**
	 * Initialise une proglet à partir d'un package et de sa configuration.
	 * 
	 * @param packageProg Le package où devrait se situer la proglet
	 * @throws ClassNotFoundException Dans le cas où la proglet n'existe pas
	 */
	public Proglet(final String packageProg) throws ClassNotFoundException{
		// On établie le package de la proglet
		if(packageProg.contains("."))
			packageName=packageProg;
		else
			packageName="org.javascool.proglets."+packageName;
		// On en déduit le nom de l'identificateur
		identifier=packageName.split(".")[packageName.split(".").length-1];
		configuration=loadConfiguration();
		if(configuration==null)
			throw new ClassNotFoundException("La proglet "+this+" n'est pas présente");
	}
	
	/**
	 * Récupère la configuration de la proglet.
	 * <p><b>Le package de la proglet doit être paramétré auparavent</b></p>
	 * <p>De plus si le retour de cette fonction est null alors on peut en déduire que la proglet
	 * n'est pas présente dans le classpath</p>
	 * @return La confguration sous forme d'objet JSON ou alors un null si la proglet n'existe pas.
	 * 
	 */
	private JSONObject loadConfiguration(){
		if(packageName==null)return null;
		try{
			// On recherche l'URL de la configuration
			final URL progletUrl=CLASS_LOADER.getResource(packageName.replace('.', '/')+"/proglet.json");
			if(progletUrl==null) // Si elle n'existe pas alors on retourne une exception
				throw new Exception();
			// On lit et retourne la configuration
			return new JSONObject(IOUtils.toString(progletUrl));			
		} catch(Exception e){
			LOG.debug("Impossible de charger la proglet "+this);
		}
		return null; // Dans le cas où aucune librairie n'as pu être chargé
	}
	
	/**
	 * Permet de déterminer si la Proglet fournit un paquet de fonctions.
	 * 
	 * @return vrai dans le cas où une classe Fonctions existe dans le package de la proglet.
	 */
	public boolean hasFunctions(){
		try{
			CLASS_LOADER.loadClass(packageName+".Functions");
			return true;
		} catch(ClassNotFoundException e){
			LOG.debug("La proglet "+this+" n'as pas de fonctions");
		}
		return false;
	}
	
	/**
	 * Permet de déterminer si la Proglet fournit un Panel (Animation graphique).
	 * 
	 * @return vrai dans le cas où une classe Panel existe dans le package de la proglet.
	 */
	public boolean hasPanel(){
		return getPanelClass()!=null;
	}
	
	/**
	 * Crée une instance du Panel de la proglet.
	 * 
	 * @return Le Panel.
	 * @throws ClassNotFoundException dans le cas où la proglet n'a pas de Panel.
	 * @throws IllegalAccessException si la classe ne peut pas être créé
	 * @throws InstantiationException si la classe ne peut pas être créé
	 */
	public Object getPanel() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		if(!hasPanel()) throw new ClassNotFoundException("La proglet "+this+" n'a pas de Panel");
		return getPanelClass().newInstance();
	}
	
	/**
	 * Permet d'obtenir la classe du Panel de la proglet.
	 * 
	 * @return La classe du Panel ou null si elle n'existe pas.
	 */
	public Class<?> getPanelClass(){
		try{
			return CLASS_LOADER.loadClass(packageName+".Panel");
		} catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Permet de déterminer si la Proglet fournit une démo avec son Panel.
	 * <p>La fonction vérifie qu'il est possible d'accéder à une méthode du
	 * nom de "demoStart" au sein du Panel de la proglet</p>
	 * @return vrai dans le cas où une démo existe dans le package de la proglet.
	 */
	public boolean hasDemo(){
		if(!hasPanel())return false;
		try {
			getPanelClass().getDeclaredMethod("demoStart");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Permet d'obtenir les éventuels import à faire pour cette proglet.
	 * <p>Les imports de la proglets sont ceux tels que les Fonctions de la proglet.</p>
	 * <p>Afin d'éviter une dépendance trop forte entre les composants de Java's Cool,
	 * la classe ne revoit pas une liste d'objet Import</p>
	 * 
	 * @return Une {@link HashMap} qui contient les Imports sous la forme <String,boolean>.
	 * 		   Cela correspond à l'import à faire et le boolean est vrai si l'Import est static.
	 */
	public HashMap<String, Boolean> getImports(){
		final HashMap<String, Boolean> imports=new HashMap<String, Boolean>();
		if(hasPanel())
			imports.put(getPackageName()+".Panel.*",true);
		if(hasFunctions())
			imports.put(getPackageName()+".Functions.*", true);
		return imports;
	}
	
	/**
	 * @see Proglet#progletName
	 */
	public String getProgletName() {
		return progletName;
	}

	/**
	 * @param progletName Le nom à mettre
	 * @see #progletName
	 */
	public void setProgletName(String progletName) {
		this.progletName = progletName;
	}

	/**
	 * @see #packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @see #identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @see #configuration
	 */
	public JSONObject getConfiguration() {
		return configuration;
	}
	
	/**
	 * Permet d'afficher la proglet dans un message.
	 */
	@Override
	public String toString(){
		if(progletName!=null)
			return progletName;
		else if(packageName!=null)
			return packageName;
		else if(identifier!=null)
			return identifier;
		else
			return "Proglet inconnu";
	}
	
	/**
	 * Définit si une Proglet correspond à une autre.
	 * <p>On peut considéré deux proglets identique si leur package et leur
	 * identificateurs sont identiques</p>
	 * @see #identifier
	 * @see #packageName
	 */
	@Override
	public boolean equals(Object o){
		if(o instanceof Proglet)
			return ((Proglet) o).getPackageName().equals(getPackageName()) &&
					((Proglet)o).getIdentifier().equals(getIdentifier());
		return false;
	}
}
