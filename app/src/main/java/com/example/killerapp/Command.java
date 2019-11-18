package com.example.killerapp;
//Reviewers expected: Scialpi, Ursino

/***
 * Interface built to make method getLastLocation modular,
 * so that it can be used to execute a certain Command
 *
 */
public interface Command<M> {
    void execute(M data);
}
