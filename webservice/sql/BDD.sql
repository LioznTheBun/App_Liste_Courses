CREATE TABLE Utilisateurs (
	id INT AUTO_INCREMENT,
	nom VARCHAR(64),
	mdp VARCHAR(255),
	PRIMARY KEY(id)
);

CREATE TABLE Rayon (
	id INT AUTO_INCREMENT,
	nom VARCHAR(255),
	PRIMARY KEY(id)
);

CREATE TABLE Produit (
	id INT AUTO_INCREMENT,
	nom VARCHAR(64),
	commentaire VARCHAR(64),
	auteurMiseEnList INT,
	misDansLeSac INT,
	rayon_id INT,
	PRIMARY KEY(id),
	FOREIGN KEY(rayon_id) REFERENCES Rayon(id),
	FOREIGN KEY(auteurMiseEnList) REFERENCES Utilisateurs(id),
	FOREIGN KEY(misDansLeSac) REFERENCES Utilisateurs(id)
);

INSERT INTO Rayon(nom) VALUES
("Ne sais pas"),
("Bazar"),
("Fruits et Légumes"),
("Produits frais"),
("Boucherie"),
("Poissonnerie"),
("Surgelés"),
("Textile"),
("Hygiène"),
("Epicerie"),
("Liquide");

INSERT INTO Utilisateurs(nom, mdp) VALUES ("Bunny", SHA2("Bunny",256));