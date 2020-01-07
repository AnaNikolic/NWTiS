DROP TABLE airplanes;
CREATE TABLE airplanes (
  id integer not null generated always as identity (start with 1, increment by 1),
  icao24 varchar(30) NOT NULL,
  firstSeen integer not null,
  estDepartureAirport varchar (10) NOT NULL,
  lastSeen integer not null,
  estArrivalAirport varchar (10) NOT NULL,
  callsign varchar (20),
  estDepartureAirportHorizDistance integer not null,
  estDepartureAirportVertDistance integer not null,
  estArrivalAirportHorizDistance integer not null,
  estArrivalAirportVertDistance integer not null,
  departureAirportCandidatesCount integer not null,
  arrivalAirportCandidatesCount integer not null,
  stored TIMESTAMP NOT NULL, 
  PRIMARY KEY (id) 
);

DROP INDEX airplanes_id;
CREATE INDEX airplanes_id ON airplanes(icao24, lastSeen);  
