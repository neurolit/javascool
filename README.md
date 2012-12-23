# Le projet Java's Cool

NOTICE: THIS README AND ALL THIS PROJECT IS WRITTEN IN FRENCH, TO GET MORE INFORMATION, CONTACT PROJECT'S OWNERS

Java's Cool est une application web conçu pour l'apprentisage de l'informatique. Elle permet de coder dans un pseudo
langage proche du Java.

Elle est basée sur des pages web enregistrés en local. Un lanceur en Java permet de lancer l'application dans un
navigateur web. Le navigateur interagit avec l'ordinateur à partir d'une applet en Java.

Vous vous trouvez actuellement sur le dépot principal de l'application qui permet de créer un espace de travail adéquate
pour développer le logiciel. Ce dernier contient aussi le fichier Maven parent ainsi que le necessaire pour généré le 
site web.

## Organisation du dépot
Le projet se divise en plusieurs sous-modules :

  * manager : Le code pour la gestion du projet
  * javascool-5 : Le receuil des fichiers web de l'application.
  * javascool-framework : Le code en Java permettant le bon fonctionnement de l'application.
  * proglet-[nom de la proglet] : Le code de la proglet (Celle dans l'organisation Java's Cool ont été validés)
  * javascool-launcher : Le site permettant le lançement de Java's Cool depuis le web.
  * javascool-proglet-builder : l'utilitaire de création des proglets.

## Choix technologiques
### L'application
Pour l'application, on a choisit l'HTML5 en raison de son essort raissant. Cependant historiquement, Java's cool a été
écrit en Java. C'est pourquoi nous nous retrouvons avec de l'HTML5 et du Java.

Bien sûr, faire de l'HTML 5 implique le fait d'avoir du JavaScript en arrière plan. Au sein de la page, le navigateur
comunique avec l'ordinateur par le biail d'applets Java cachés. Le JavaScript est completé par la librairie jQuery et
le style est pris en charge par Bootstrap.

### Le projet
Il est stocké sur GitHub pour de facilités de partage. De nombreuses commandes de construction du projet sont
actuellement executés depuis des scripts shell. le projet est donc compilable dans l'état actuelleque sous linux. Dans
 un souci de compatibilité, il serait bon d'utilisé ant car c'est une solution multi-système.

Cependant la compatibilité aux Makefile doit être respecté, mais il ne doivent plus être obligatoires.
Cela revient à dire que les make doivent appeler des tâches ant, maven ou en Java pure.

Dans les choix technologiques, nous ajoutons Maven en raison de sa modularité et de ses tâche pré-integrés. De plus, il
permet de gérer tout le système de dépendance au niveau des Jars.

#### Environnement de développement intégré
Pour développer ce projet, on favorise IntelliJ IDEA mais on fait tout en sorte qu'il soit possible de modifier le code
sans cet IDE et même avec un autre.

Du fait de la compatibilité Maven, le projet supprote aussi bien IDEA que Eclipse. Il n'est pas recommender d'utiliser
NetBeans. Pour la section du style du code, tous les codes doivents être passé au style Eclipse (mode Convention
Eclipse). Pour cela les tâche maven doivent l'executer au clean.

## Guide de démarrage rapide pour les développeurs
[En cours de refonte ...]

## Contact
  - INRIA : http://www.inria.fr
  - Java's Cool : http://www.javascool.fr

## Licence
![GNU GPL V3](http://www.gnu.org/graphics/gplv3-127x51.png)
