<?php

include("tools/DatabaseLinker.php");

ini_set('display_errors', 'on');
ini_set('display_startup_errors', 'on');
error_reporting(E_ALL);


// Analyser l'URL pour déterminer l'action à effectuer
$action = isset($_GET['action']) ? $_GET['action'] : null;

// Exécuter l'action appropriée en fonction de l'URL
switch ($action) {
    case 'connexionUtilisateur':
        // Vérifier les paramètres requis
        if (isset($_GET['nom']) && isset($_GET['mdp'])) {
            // Appeler la fonction de connexion utilisateur avec les paramètres
            echo connexionUtilisateur($_GET['nom'], $_GET['mdp']);

        } else {
            echo json_encode(['error' => 'Paramètres manquants pour la connexion utilisateur']);
        }
        break;

    case 'rayons':
        // Appeler la fonction pour obtenir la liste des rayons
        echo getRayons();
        break;

    case 'suppr':
        // Appeler la fonction pour réinitialiser la liste de courses et du cadis
        echo suppr();
        break;

    case 'ajouter':
        // Analyser l'entrée JSON du corps de la requête
        $inputJSON = file_get_contents('php://input');
        $input = json_decode($inputJSON, true);
    
        $produit = [
            'nom' => $input['nom'],
            'commentaire' => $input['commentaire'],
            'auteurMiseEnList' => $input['auteurMiseEnList'],
            'rayon_id' => $input['rayon_id']
        ];
        // Appeler la fonction pour ajouter le produit avec les paramètres
        echo ajouterProduit($produit);
        break;

    case 'courses':
        // Appeler la fonction pour obtenir la liste de courses
        echo getCourses();
        break;

    case 'cadis':
        // Appeler la fonction pour obtenir la liste du cadis
        echo getCadis();
        break;
        
    case 'transferer':
        $inputJSON = file_get_contents('php://input');
        $input = json_decode($inputJSON, true);
    
        $info = [
            'id_produit' => $input['id_produit'],
            'utilisateur' => $input['utilisateur']
        ];
         echo transfererProduit($info["utilisateur"], $info["id_produit"]);

        break;
    case 'deconnexion':
        // Appeler la fonction pour déconnecter l'utilisateur
        echo deconnexion();
        break;

    default:
        echo json_encode(['error' => 'Action non reconnue']);
}

// Fonction pour connecter un utilisateur
function connexionUtilisateur($nom, $mdp)
{

    $conn = DatabaseLinker::getConnexion();
    $query = "SELECT * FROM Utilisateurs WHERE nom = ? AND mdp = SHA2(?, 256)";
    $stmt = $conn->prepare($query);
    $stmt->execute([$nom, $mdp]);
    $utilisateur = $stmt->fetch(PDO::FETCH_ASSOC);
    $conn = null;

    if ($utilisateur) {
        return json_encode(['request' => 'connexion', 'result' => true, 'idUtilisateur' => $utilisateur["id"]]);
    } else {
        return json_encode(['request' => 'connexion', 'result' => false]);
    }
}

// Fonction pour obtenir la liste des rayons
function getRayons()
{
    $conn = DatabaseLinker::getConnexion();
    $query = "SELECT * FROM Rayon";
    $stmt = $conn->query($query);
    $rayons = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $conn = null;
    return json_encode(['request' => 'rayons', 'result' => $rayons]);
}

// Fonction pour réinitialiser la liste de courses et du cadis
function suppr()
{
    $conn = DatabaseLinker::getConnexion();
    $queryCadis = "DELETE FROM Produit";
    $stmtCadis = $conn->prepare($queryCadis);
    $stmtCadis->execute();
    $conn = null;
    return json_encode(['request' => 'reset', 'result' => true]);
}

// Fonction pour ajouter un produit dans la liste de courses
function ajouterProduit($produit)
{
    $conn = DatabaseLinker::getConnexion();
    $query = "INSERT INTO Produit (nom, commentaire, auteurMiseEnList, rayon_id) 
                VALUES (?, ?, ?, ?)";
    $stmt = $conn->prepare($query);
    $stmt->execute([$produit['nom'], $produit['commentaire'], $produit['auteurMiseEnList'], $produit['rayon_id']]);
    $conn = null;
    return json_encode(['request' => 'ajouter', 'result' => true]);
}

// Fonction pour obtenir la liste de courses
function getCourses()
{
    $conn = DatabaseLinker::getConnexion();
    $query = "SELECT * FROM Produit WHERE misDansLeSac IS NULL";
    $stmt = $conn->query($query);
    $courses = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $conn = null;
    return json_encode(['request' => 'courses', 'result' => $courses]);
}

// Fonction pour obtenir la liste du cadis
function getCadis()
{
    $conn = DatabaseLinker::getConnexion();
    $query = "SELECT * FROM Produit WHERE misDansLeSac IS NOT NULL";
    $stmt = $conn->query($query);
    $cadis = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $conn = null;
    return json_encode(['request' => 'cadis', 'result' => $cadis]);
}

// Fonction pour transférer un produit de la liste dans le cadis
function transfererProduit($idUtilisateur, $idProduit)
{
    $conn = DatabaseLinker::getConnexion();
    $query = "UPDATE Produit SET misDansLeSac = ? WHERE id = ?";
    $stmt = $conn->prepare($query);
    $stmt->execute([$idUtilisateur ,$idProduit]);
    $conn = null;
    return json_encode(['request' => 'transferer', 'result' => true]);
}

// Fonction pour déconnecter la session utilisateur
function deconnexion()
{
    return json_encode(['request' => 'deconnexion', 'result' => true]);
}
