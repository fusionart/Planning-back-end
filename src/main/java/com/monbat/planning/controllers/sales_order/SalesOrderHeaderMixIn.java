package com.monbat.planning.controllers.sales_order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.monbat.vdm.namespaces.apisalesordersrv.ItemPartner;
import com.monbat.vdm.namespaces.apisalesordersrv.SalesOrderItem;

import java.util.List;

public abstract class SalesOrderHeaderMixIn {
    @JsonIgnore
    private List<SalesOrderItem> toItem;

    @JsonProperty("to_Item")
    private JsonNode toItemNode;

    @JsonIgnore
    private List<ItemPartner> toPartner;

    @JsonProperty("to_Partner")
    private JsonNode toPartnerNode;

    // Add similar ignores for all other problematic fields
    @JsonIgnore
    private Object toPaymentPlanItemDetails;

    @JsonProperty("to_PaymentPlanItemDetails")
    private JsonNode toPaymentPlanItemDetailsNode;

    @JsonIgnore
    private Object toPrecedingProcFlowDoc;

    @JsonProperty("to_PrecedingProcFlowDoc")
    private JsonNode toPrecedingProcFlowDocNode;

    @JsonIgnore
    private Object toPricingElement;

    @JsonProperty("to_PricingElement")
    private JsonNode toPricingElementNode;

    @JsonIgnore
    private Object toRelatedObject;

    @JsonProperty("to_RelatedObject")
    private JsonNode toRelatedObjectNode;

    @JsonIgnore
    private Object toSubsequentProcFlowDoc;

    @JsonProperty("to_SubsequentProcFlowDoc")
    private JsonNode toSubsequentProcFlowDocNode;

    @JsonIgnore
    private Object toText;

    @JsonProperty("to_Text")
    private JsonNode toTextNode;
}