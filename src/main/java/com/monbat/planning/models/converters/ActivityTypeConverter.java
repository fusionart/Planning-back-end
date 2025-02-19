package com.monbat.planning.models.converters;

import com.google.gson.Gson;
import com.monbat.planning.models.entities.ActivityType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActivityTypeConverter implements AttributeConverter<ActivityType, String> {

    private final Gson gson = new Gson();

    @Override
    public String convertToDatabaseColumn(ActivityType activityType) {
        return gson.toJson(activityType);
    }

    @Override
    public ActivityType convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData, ActivityType.class);
    }
}
