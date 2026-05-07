package com.example.ttcrs.constant;

/**
 * Transport request type:
 * OF = Outbound Full  (xuất hàng đầy container)
 * IF = Inbound Full   (nhập hàng đầy container)
 * OE = Outbound Empty (xuất container rỗng)
 * IE = Inbound Empty  (nhập container rỗng)
 */
public enum RequestType {
    OF,
    IF,
    OE,
    IE
}
