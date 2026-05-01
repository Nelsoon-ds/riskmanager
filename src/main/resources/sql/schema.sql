CREATE DATABASE IF NOT exists RiskLensDB CHARACTER SET utf8;

USE RiskLensDB;

CREATE TABLE if not exists users (
                                     user_id int auto_increment primary key,
                                     oauth_id varchar(255) not null ,
                                     provider varchar(100) not null,
                                     name varchar(255),
                                     email varchar(255),
                                     created_at DATETIME not null,
                                     unique key uq_oauth (oauth_id, provider)
);


CREATE TABLE RiskAssessment (
                                assessment_id int auto_increment,
                                user_id int,
                                overall_severity varchar(1000),
                                summary varchar(2000),
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                image_path VARCHAR(500) NULL,
                                primary key (assessment_id),
                                foreign key (user_id) references Users(user_id)
);

CREATE TABLE Hazard (
                        hazard_id int auto_increment,
                        name Varchar(1000),
                        severity Varchar(20),
                        description Varchar(5000),
                        assessment_id int,
                        bounding_box TEXT,
                        primary key (hazard_id),
                        foreign key (assessment_id) references RiskAssessment(assessment_id)
);

CREATE TABLE StandardReference (
                                   standard_ref_id int auto_increment,
                                   section varchar(2000),
                                   name varchar(500),
                                   relevance varchar(2000),
                                   hazard_id int,
                                   primary key (standard_ref_id),
                                   foreign key (hazard_id) references Hazard(hazard_id)
);

CREATE TABLE Recommendation (
                                rec_id int auto_increment,
                                rec_description varchar(2000),
                                hazard_id int,
                                primary key (rec_id),
                                foreign key (hazard_id) references Hazard(hazard_id)
);





