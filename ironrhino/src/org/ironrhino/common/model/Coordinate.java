package org.ironrhino.common.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.ironrhino.core.metadata.NotInJson;

@Embeddable
public class Coordinate implements Serializable {

	private static final long serialVersionUID = 5828814302557010566L;

	private Double latitude;

	private Double longitude;

	public Coordinate() {

	}

	public Coordinate(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(String latLng) {
		setLatLngAsString(latLng);
	}

	public Coordinate(String latitude, String longitude) {
		this.latitude = parseLatOrLong(latitude);
		this.longitude = parseLatOrLong(longitude);
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@NotInJson
	public void setLatLngAsString(String latLng) {
		if (latLng == null || latLng.trim().length() == 0) {
			this.latitude = null;
			this.longitude = null;
		} else {
			String[] arr = latLng.split(",");
			this.latitude = parseLatOrLong(arr[0]);
			this.longitude = parseLatOrLong(arr[1]);
		}
	}

	public String getLatLngAsString() {
		if (latitude != null && longitude != null)
			return latitude + "," + longitude;
		else
			return null;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return getLatLngAsString();
	}

	public static Double parseLatOrLong(String input) {
		try {
			return Double.valueOf(input);
		} catch (Exception e) {
			int i = input.indexOf('°');
			double d = Double.valueOf(input.substring(0, i));
			input = input.substring(i + 1);
			i = input.indexOf('\'');
			if (i > 0) {
				d += Double.valueOf(input.substring(0, i)) / 60;
				input = input.substring(i + 1);
				i = input.indexOf('"');
				if (i > 0) {
					d += Double.valueOf(input.substring(0, i)) / (60 * 60);
					input = input.substring(i + 1).trim().toUpperCase();
					if (input.equals("S") || input.equals("W"))
						d = 0 - d;
				}
			}
			return d;
		}
	}

}
