import re

ios_code = """
    "Wholesome fruit": "health.phrase.wholesome_fruit",
    "Mostly whole fruit with fiber and potassium.": "health.phrase.whole_fruit_fiber_potassium",
    "Nutrient rich": "health.phrase.nutrient_rich",
    "Potassium vitamin B6 fiber": "health.phrase.potassium_b6_fiber",
    "Natural sugar": "health.phrase.natural_sugar",
    "Sugar spike !": "health.phrase.sugar_spike",
    "Sugar spike!": "health.phrase.sugar_spike",
    "Raises blood sugar if overeat": "health.phrase.raises_blood_sugar",
    "Fiber and key nutrients": "health.phrase.fiber_and_nutrients",
    "Moderate natural sugar": "health.phrase.moderate_sugar",
    "High sodium": "health.phrase.high_sodium",
    "Ultra-processed": "health.phrase.ultra_processed",
    "Added sugars": "health.phrase.added_sugars",
    "Healthy fats": "health.phrase.healthy_fats",
    "Good protein source": "health.phrase.protein_source",
    "Broccoli": "health.phrase.broccoli",
    "Carrot": "health.phrase.carrot",
    "Green bean": "health.phrase.green_bean",
    "Potato": "health.phrase.potato",
    "Vegetable oil": "health.phrase.vegetable_oil",
    "Salt": "health.phrase.salt",
    "Vegetables in general": "health.phrase.vegetables_general",
    "Lots of fiber and vitamins, but there is oil and salt.": "health.phrase.fiber_vitamins_oil_salt",
    "Antioxidants": "health.phrase.antioxidants",
    "Vision and skin": "health.phrase.vision_skin",
    "Vitamin A beta carotene": "health.phrase.vitamin_a_beta_carotene",
    "Vitamins C K folate": "health.phrase.vitamins_c_k_folate",
    "Satiety": "health.phrase.satiety",
    "Fiber folate vitamin C": "health.phrase.fiber_folate_vitamin_c",
    "Energy": "health.phrase.energy",
    "Potassium carbohydrates for energy": "health.phrase.potassium_carbs_energy",
    "Excess calories": "health.phrase.excess_calories",
    "High calorie content": "health.phrase.high_calorie",
    "Excess salt": "health.phrase.excess_salt",
    "May increase sodium": "health.phrase.may_increase_sodium",
    "broccoli": "health.phrase.broccoli",
    "carrot": "health.phrase.carrot",
    "green bean": "health.phrase.green_bean",
    "potato": "health.phrase.potato",
    "vegetable oil": "health.phrase.vegetable_oil",
    "salt": "health.phrase.salt",
"""

out = []
for line in ios_code.splitlines():
    line = line.strip()
    if not line: continue
    parts = line.split(":")
    if len(parts) >= 2:
        key = parts[0].strip()
        val = parts[1].strip().rstrip(",")
        out.append(f"        {key} to {val},")

print("\n".join(out))
