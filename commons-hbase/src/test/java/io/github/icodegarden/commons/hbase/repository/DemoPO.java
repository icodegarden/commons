package io.github.icodegarden.commons.hbase.repository;

import io.github.icodegarden.commons.lang.IdObject;
import lombok.Data;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Data
public class DemoPO implements IdObject<String> {

	private String id;
	private String model;
	private Long vehicleModelId;
	private String vin;
}
