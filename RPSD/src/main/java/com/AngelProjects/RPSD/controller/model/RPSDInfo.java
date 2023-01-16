package com.AngelProjects.RPSD.controller.model;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RPSDInfo {
    ArrayList<String> players;
    String name;
    String state;
    String receiver;
    String sender;
    Boolean challenging;
    Boolean challenged;
    Boolean connected;
    int numTurns;
    String key;

    Player player1Info;
    Player player2Info;
    String roundWinner;
    String winner;
}
