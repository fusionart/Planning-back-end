package com.monbat.planning.controllers.sales_order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class SalesOrderItemMixIn {
    @JsonIgnore
    private Object toPartner;
    @JsonIgnore
    private Object toBillingPlan;
    @JsonIgnore
    private Object toPrecedingProcFlowDocItem;
    @JsonIgnore
    private Object toPricingElement;
    @JsonIgnore
    private Object toRelatedObject;
    @JsonIgnore
    private Object toSalesOrder;
    @JsonIgnore
    private Object toScheduleLine;
    @JsonIgnore
    private Object toSubsequentProcFlowDocItem;
    @JsonIgnore
    private Object toText;

    // Corresponding JSON properties
    @JsonProperty("to_Partner") private JsonNode toPartnerNode;
    @JsonProperty("to_BillingPlan") private JsonNode toBillingPlanNode;
    @JsonProperty("to_PrecedingProcFlowDocItem") private JsonNode toPrecedingProcFlowDocItemNode;
    @JsonProperty("to_PricingElement") private JsonNode toPricingElementNode;
    @JsonProperty("to_RelatedObject") private JsonNode toRelatedObjectNode;
    @JsonProperty("to_SalesOrder") private JsonNode toSalesOrderNode;
    @JsonProperty("to_ScheduleLine") private JsonNode toScheduleLineNode;
    @JsonProperty("to_SubsequentProcFlowDocItem") private JsonNode toSubsequentProcFlowDocItemNode;
    @JsonProperty("to_Text") private JsonNode toTextNode;
}