package com.prhythm.ssjv001.service.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfo {
    private String ticketId;
    private String command;
    private String type;
    private String status;
    private String submitter;
    private Date createTime;
}
