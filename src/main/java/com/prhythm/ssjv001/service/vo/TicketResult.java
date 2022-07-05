package com.prhythm.ssjv001.service.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResult {
    private String ticketId;
    private String message;
    private String type;
    private String command;
    private Date created;
}
