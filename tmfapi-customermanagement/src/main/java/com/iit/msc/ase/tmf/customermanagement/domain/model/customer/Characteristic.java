package com.iit.msc.ase.tmf.customermanagement.domain.model.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Dhanushka Sampath
 * @version 1.0
 * @since 2021.02.14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Characteristic {

    private long primaryId;
    private String parentEntity;
    private String parentId;
    private String immediateParent;
    private String immediateParentId;
    private String baseType;
    private String type;
    private String valueType;
    private String value;
    private String name;
    private String schemaLocation;
}
