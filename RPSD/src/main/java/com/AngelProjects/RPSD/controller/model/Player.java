package com.AngelProjects.RPSD.controller.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Player {
    public String decision;
    public  String move;
    public int score;
    public String name;

    Boolean receivedMove;

}
