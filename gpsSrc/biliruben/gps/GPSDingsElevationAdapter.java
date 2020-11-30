package biliruben.gps;

import java.math.BigDecimal;

import net.sourceforge.gpstools.dem.DEMException;
import net.sourceforge.gpstools.dem.ElevationModel;

import biliruben.gps.api.ElevationSource;

public class GPSDingsElevationAdapter implements ElevationSource {

	private ElevationModel _model;

	public GPSDingsElevationAdapter (ElevationModel model) {
		_model = model;
	}
	
	@Override
	public BigDecimal getElevation(BigDecimal lat, BigDecimal lon) {
		
		try {
			return _model.getElevation(lat, lon);
		} catch (DEMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
