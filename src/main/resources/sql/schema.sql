CREATE DATABASE RiskLensDB CHARACTER SET utf8;

USE RiskLensDB;


CREATE TABLE RiskAssessment (
    assessment_id SMALLINT auto_increment,
    overall_severity varchar(1000) default 0,
    summary varchar(2000) default 0,
    hazard_id SMALLINT,
    primary key (assessment_id)
);

CREATE TABLE Hazard (
    hazard_id SMALLINT auto_increment,
    name Varchar(100),
    severity Varchar(20),
    description Varchar(1000),
    assessment_id smallint,
    standard_reference_id smallint,
    rec_id smallint,
    bounding_box double,
    primary key (hazard_id),
    foreign key (assessment_id) references RiskAssessment(assessment_id)
);

CREATE TABLE StandardReference (
    standard_ref_id smallint auto_increment,
    section varchar(1000),
    relevance varchar(100),
    hazard_id smallint,
    primary key (standard_ref_id),
    foreign key (hazard_id) references Hazard(hazard_id)
);

CREATE TABLE Recommendation (
    rec_id smallint auto_increment,
    rec_description varchar(2000),
    hazard_id smallint,
    primary key (rec_id),
    foreign key (hazard_id) references Hazard(hazard_id)
);




