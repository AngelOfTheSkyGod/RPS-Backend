package com.AngelProjects.RPSD.controller;


import com.AngelProjects.RPSD.controller.model.Player;
import com.AngelProjects.RPSD.controller.model.RPSDLogic;
import com.AngelProjects.RPSD.controller.model.RPSDInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class RPSDController {
    ArrayList<String> players = new ArrayList<>();
    ConcurrentHashMap<String, RPSDInfo> matches = new ConcurrentHashMap <>();
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private final RPSDLogic logic = new RPSDLogic();

    @MessageMapping("/global")
    @SendTo("/requests/newPlayer")
    public RPSDInfo receivePublicMessage(@Payload RPSDInfo info){
        System.out.println("connected? : " + info.getConnected());
        if (info.getConnected() && !players.contains(info.getName()) && !Objects.equals("ALREADY TAKEN", info.getName())){
            players.add(info.getName());
        }else if (players.contains(info.getName()) && !info.getConnected()){
            players.remove(players.indexOf(info.getName()));
        }
        info.setPlayers(players);
        System.out.println("SENDING BACK: " + info);
        return info;
    }

    @MessageMapping("/leaveGame")
    @SendTo("/requests/leaver")
    public RPSDInfo receivePlayerLeft(@Payload RPSDInfo info){

        matches.forEach((key, value)->{
            if (Objects.equals(info.getName(), value.getPlayer1Info().getName()) ||
                    Objects.equals(info.getName(), value.getPlayer2Info().getName())){
                matches.remove(key);
            }
        });
        players.remove(players.indexOf(info.getName()));
        info.setPlayers(players);
        return info;
    }
    @MessageMapping("/challengePlayer")
    public RPSDInfo receiveUpdateRequest(@Payload RPSDInfo info){
        System.out.println("challenged by: " + info.getSender() + "challenging: " + info.getReceiver());
        Player newPlayer = new Player();
        newPlayer.setName(info.getSender());
        info.setPlayer1Info(newPlayer);
        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/challenged", info);
        return info;
    }

    @MessageMapping("/cancelChallengePlayer")
    public RPSDInfo receiveCancelChallenge(@Payload RPSDInfo info){
        info.setChallenged(false);
        info.setChallenging(false);
        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/cancelledChallenge", info);
        return info;
    }

    @MessageMapping("/declineChallenge")
    public RPSDInfo receiveDeclineChallenge(@Payload RPSDInfo info){
        info.setChallenged(false);
        info.setChallenging(false);
        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/declinedChallenge", info);
        return info;
    }

    @MessageMapping("/acceptChallenge")
    public RPSDInfo receiveAcceptChallenge(@Payload RPSDInfo info){
        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/acceptedChallenge", info);
        return info;
    }

    //sends wager from player x to player y
    @MessageMapping("/sendWager")
    public RPSDInfo receiveWager(@Payload RPSDInfo info){

        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/sendWager", info);
        return info;
    }

    //syncs up both players and starts their games.
    @MessageMapping("/startGame")
    public RPSDInfo receiveStartGame(@Payload RPSDInfo info){
        simpMessagingTemplate.convertAndSendToUser("garbage","/startGame", info);

        if (!Objects.equals(info.getPlayer1Info().getDecision(), "") && !Objects.equals(info.getPlayer2Info().getDecision(), "")){
            simpMessagingTemplate.convertAndSendToUser(info.getPlayer1Info().name, "/startGame", info);
            simpMessagingTemplate.convertAndSendToUser(info.getPlayer2Info().name, "/startGame", info);
            matches.put(info.getPlayer1Info().getName(), info);
            System.out.println("BOTH HAVE MADE THEIR DECISIONS. STARTING GAME");
        }
        return info;
    }

    ///in charge of dealing with receiving moves that players make.
    //each match is stored within a hashmap where the key is player1's name.
    //when both players make a choice, they receive a message with the updated object.
    @MessageMapping("/private-move")

    public void receivePrivateMove(@Payload RPSDInfo info){
        RPSDInfo data = matches.get(info.getPlayer1Info().getName());
        if (data == null) {
            return;
        }
        if (Objects.equals(info.getSender(), info.getPlayer1Info().getName())){
            data.getPlayer1Info().setReceivedMove(true);
            data.getPlayer1Info().setMove(info.getPlayer1Info().getMove());
//            System.out.println("player 1 has chosen: " + info.getPlayer1Info().getMove());
        }else{
            data.getPlayer2Info().setReceivedMove(true);
            data.getPlayer2Info().setMove(info.getPlayer2Info().getMove());
//            System.out.println("player 2 has chosen: " + info.getPlayer2Info().getMove());
        }

        matches.put(info.getPlayer1Info().getName(), data);

        //check if both players are done with their choices.
        if (data.getPlayer1Info().getReceivedMove() && data.getPlayer2Info().getReceivedMove()){
            RPSDInfo evaluated = logic.evaluateMove(data);
            evaluated.setNumTurns(evaluated.getNumTurns() + 1);
            simpMessagingTemplate.convertAndSendToUser(evaluated.getPlayer1Info().getName(), "/sendMove", evaluated);
            simpMessagingTemplate.convertAndSendToUser(evaluated.getPlayer2Info().getName(), "/sendMove", evaluated);
            evaluated.getPlayer1Info().setReceivedMove(false);
            evaluated.getPlayer2Info().setReceivedMove(false);
            evaluated.getPlayer1Info().setMove("");
            evaluated.getPlayer2Info().setMove("");
            matches.put(info.getPlayer1Info().getName(), evaluated);
            //sendMove
        }
        ///match has ended.
        if (!Objects.equals(matches.get(info.getPlayer1Info().getName()).getWinner(), "")){
            matches.remove(info.getPlayer1Info().getName());
        }


    }

    @MessageMapping("/private-updatePlayers")

    public RPSDInfo receiveUpdatePlayersRequest(@Payload RPSDInfo info){
        info.setPlayers(players);
        System.out.println("RECEIVED REQUEST TO UPDATE PLAYERS FROM: " + info.getName());

        simpMessagingTemplate.convertAndSendToUser(info.getReceiver(), "/updatePlayers", info);
        return info;
    }
}
