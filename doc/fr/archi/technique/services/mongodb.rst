Mongodb
#######

Base de données dédié aux données métier


Type :
	COTS

Données stockées :
	* Données d'archives
	* Journaux métier
	* Référentiels métier

Typologie de consommation de resources :
	* CPU : moyenne
	* Mémoire : forte
	* Réseau : forte
	* Disque : forte


Architecture de déploiement
===========================

Architecture 1 noeud
--------------------

* 1 serveur mongodb:

    - 1 noeud mongod


Architecture distribuée
-----------------------

Une architecture MongoDB distribuée utilise les notions suivantes :

* Sharding
    - Mongodb utilise la sharding pour scaler la base de données (scalabilité horizontale)
    - Le sharding distribue les données à travers les n partitions physiques (shards) dont le cluster est composé
    - Bien choisir la clé de sharding est primordial pour une répartition égale des documents insérés dans les différents shards
    - Chaque shard est composé d'un Replica Set

* Replica Set (RS)
    - Les Replica Set assurent la haute disponibilité de Mongodb
    - Un Replica Set est composé d'un noeud primaire et de deux noeuds secondaires. (Règles Mongodb de production)
    - L'écriture se fait obligatoirement sur le noeud primaire

* Replica Set de config
    - Un Replica Set est dédié pour le stockage de la configuration du cluster
    - Comme tous les autres Replica Set, il est recommandé de le peupler d'au moins 3 noeuds

* Routeur de requêtes
    - Le routeur mongos permet de rediriger une requête sur le ou les shards requis, en fonction de la clé de sharding ; il agit comme coordinateur de requête.

Une architecture MongoDB distribuée comprend 3 types de noeuds différents :

* mongod : stockent les données des replica set métier ;
* mongos : routent les requêtes ;
* mongoc : stockent les données d'état et de configuration du cluster (ces noeuds utilisent en fait un moteur mongod, mais pour un replica set particulier : le replica set de configuration).

.. figure:: images/mongo-cluster.*
    :align: center

    Déploiement d'un cluster Mongo DB avec sharding.

L'architecture proposée dans le cadre de VITAM consiste à séparer les noeuds liés au routage des requêtes et de gestion du cluster d'une part (donc de colocaliser mongos et mongoc), avec les noeuds de stockage des données (mongod) d'autre part.

Ainsi, avec n shards et r noeuds par replica set (cluster), on obtient le déploiement suivant :

* 3 serveurs config / service, chacun hébergeant:

    - 1 noeud mongos (service)
    - 1 noeud mongoc (Replica Set de configuration)

* n x r serveurs, chacun hébergeant:

    - 1 noeud mongod

.. note:: Dans le cadre de cette version du système VITAM, seul un shard sera configuré, mais de telle manière à pouvoir instancier d'autres shards sans modification conséquente du déploiement et de la configuration.

Ports utilisés
--------------

Les ports utilisés par mongodb sont les suivants:

* ``tcp:27017`` : Port de communication pour les noeuds mongos
* ``tcp:27018`` : Port d'écoute des noeuds du Replica Set de config (mongoc)
* ``tcp:27019`` : Port d'écoute des noeuds du Replica Set de données (mongod)

.. Monitoring
.. ==========
.. 
