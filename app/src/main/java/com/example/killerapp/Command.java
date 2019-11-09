package com.example.killerapp;

/***
 * Interface built to make method MainActivity.getLastLocation modular,
 * so that it can be used to execute a certain Command
 *
 */
public interface Command<M> {
    void execute(M data);
}