package com.roman.ttu.client.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class RecognizedReceiptData implements Serializable{

    public String recognitionId;
    public String companyName;
    public String companyId;
    public BigDecimal totalCost;
    public String currency;
}
