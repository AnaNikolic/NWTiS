DROP TABLE myairports;
CREATE TABLE myairports (
  ident varchar(10) NOT NULL,
  name varchar(255) NOT NULL,
  iso_country varchar(30),  
  coordinates varchar(30) NOT NULL,  
  stored TIMESTAMP NOT NULL,  
  PRIMARY KEY (ident),
  FOREIGN KEY (ident) REFERENCES airports (ident)
);