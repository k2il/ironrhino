package org.ironrhino.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.ironrhino.core.model.Model;
import org.ironrhino.core.util.JSONUtils;
import org.json.JSONObject;
import org.junit.Test;

public class JsonUtilsTest {

	@Test
	public void testMapToJSON() throws Exception {
		Model model = new Model();
		model.setId("id");
		model.setName("名字");
		model.setValue("value");
		model.setPassword("password");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("model", model);
		String json = JSONUtils.mapToJSON(map);
		JSONObject jo = new JSONObject(json);
		JSONObject mo = (JSONObject) jo.get("model");
		assertEquals(model.getId(), (mo.getString("id")));
		assertEquals(model.getName(), (mo.getString("name")));
		assertEquals(model.getValue(), (mo.getString("value")));
		assertEquals(false, (mo.getBoolean("new")));
		assertFalse(mo.has("null"));
		assertFalse(mo.has("password"));
	}
}
