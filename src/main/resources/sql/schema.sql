CREATE DATABASE IF NOT exists RiskLensDB CHARACTER SET utf8;

 #USE RiskLensDB;


CREATE TABLE RiskAssessment (
                                assessment_id SMALLINT auto_increment,
                                overall_severity varchar(1000),
                                summary varchar(2000),
                                primary key (assessment_id)
);

CREATE TABLE Hazard (
                        hazard_id SMALLINT auto_increment,
                        name Varchar(1000),
                        severity Varchar(20),
                        description Varchar(5000),
                        assessment_id smallint,
                        bounding_box TEXT,
                        primary key (hazard_id),
                        foreign key (assessment_id) references RiskAssessment(assessment_id)
);

CREATE TABLE StandardReference (
                                   standard_ref_id smallint auto_increment,
                                   section varchar(2000),
                                   name varchar(500),
                                   relevance varchar(2000),
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

# insert into RiskAssessment (overall_severity, summary, hazard_id) VALUES ()




