package org.onap.rapp.datacollector.entity.fileready;

import org.onap.rapp.datacollector.entity.ves.CommonEventHeader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * Object for storing PM Bulk File information coming from PM Mapper
 */
@JsonTypeName("event")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@Getter
public class FileReadyEvent {

    private CommonEventHeader commonEventHeader;
    private MeasDataCollection measDataCollection;
}
