package com.AngelProjects.RPSD.controller.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RPSDLogic {
    Map<String, String> isWeakTo;

    public RPSDLogic(){
        isWeakTo = new HashMap<String, String>();
        isWeakTo.put("scissors", "rock");
        isWeakTo.put("rock", "paper");
        isWeakTo.put("paper", "scissors");
    }

    public Boolean isTie(RPSDInfo info){
        return Objects.equals(info.getPlayer1Info().getMove(), info.getPlayer2Info().getMove());
    }
    public RPSDInfo evaluateMove(RPSDInfo info){
        if (isWeakTo.containsKey(info.player1Info.move) && Objects.equals(isWeakTo.get(info.player1Info.move),
                info.player2Info.move)){
            info.player2Info.score++;
            info.roundWinner = info.player2Info.name;
            System.out.println(info.player1Info.move + "is weak to: " + info.player2Info.move);
        }else if (isWeakTo.containsKey(info.player2Info.move) && Objects.equals(isWeakTo.get(info.player2Info.move),
                info.player1Info.move)){
            info.player1Info.score++;
            info.roundWinner = info.player1Info.name;
            System.out.println(info.player2Info.move + "is weak to: " + info.player1Info.move);
        }else if (info.getPlayer2Info().getReceivedMove() && info.getPlayer1Info().getReceivedMove()){
            info.roundWinner = "no one. This round is a tie";
            System.out.println("p1: " + info.player1Info.move + " p2: " + info.player2Info.move );
        }

        if (info.player1Info.score >= 2){
            info.winner = info.player1Info.name;
            info.state = "endscr";
        }else if (info.player2Info.score >= 2){
            info.winner = info.player2Info.name;
            info.state = "endscr";
        }else if (info.numTurns >= 5){
            info.winner = "no one";
            info.state = "endscr";
        }
        return info;
    }
}
