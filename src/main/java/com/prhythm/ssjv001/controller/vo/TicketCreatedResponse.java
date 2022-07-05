package com.prhythm.ssjv001.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketCreatedResponse {
    private String ticketId;
    private String message;
    private String type;
    private String command;
    private Date created;
}
