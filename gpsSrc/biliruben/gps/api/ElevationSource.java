package biliruben.gps.api;

import java.math.BigDecimal;

public interface ElevationSource {

	public BigDecimal getElevation(BigDecimal lat, BigDecimal lon);
	
}
