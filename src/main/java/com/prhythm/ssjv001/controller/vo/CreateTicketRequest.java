package com.prhythm.ssjv001.controller.vo;

import lombok.Data;

@Data
public class CreateTicketRequest {
    private String type;
    private String command;
    private String submitter;
}
