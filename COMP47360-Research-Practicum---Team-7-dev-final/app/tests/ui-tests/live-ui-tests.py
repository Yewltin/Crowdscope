import pytest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options

BASE_URL = "http://137.43.49.23/crowdScope.html"

@pytest.fixture(scope="module")
def browser():
    options = Options()
    options.add_argument("--headless")  # Running in background
    driver = webdriver.Chrome(options=options)
    yield driver
    driver.quit()

def test_page_load(browser):
    browser.get(BASE_URL)
    assert "CrowdScope" in browser.title

def test_search_bar_and_input(browser):
    browser.get(BASE_URL)
    search_input = browser.find_element(By.ID, "searchInput")
    search_input.send_keys("museum")
    assert search_input.get_attribute("value") == "museum"

def test_day_buttons_exist(browser):
    browser.get(BASE_URL)
    for day in ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]:
        assert browser.find_element(By.XPATH, f"//button[contains(text(), '{day}')]").is_displayed()

def test_category_buttons(browser):
    browser.get(BASE_URL)
    for category in ["Gallery", "Museum", "Park", "Restaurant", "Shopping"]:
        browser.find_element(By.XPATH, f"//button[contains(text(), '{category}')]").click()

def test_time_slider(browser):
    browser.get(BASE_URL)
    slider = browser.find_element(By.ID, "timeSlider")
    assert slider.get_attribute("type") == "range"

def test_alt_tags_on_images(browser):
    browser.get(BASE_URL)
    images = browser.find_elements(By.TAG_NAME, "img")
    for img in images:
        assert img.get_attribute("alt") != ""
