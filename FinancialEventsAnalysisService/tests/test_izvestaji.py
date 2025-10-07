"""
Test skripta za generator izveštaja

Testira sve funkcionalnosti generatora PDF izveštaja.
"""

import requests
import json
import time
from datetime import datetime

# Konfiguracija
BASE_URL = "http://localhost:8001/api"
IZVESTAJI_URL = f"{BASE_URL}/izvestaji"


def print_separator(title=""):
    """Štampa separator"""
    print("\n" + "="*80)
    if title:
        print(f"  {title}")
        print("="*80)


def test_1_generiši_osnovni_izvestaj():
    """TEST 1: Generisanje osnovnog izveštaja bez filtera"""
    print_separator("TEST 1: Generisanje osnovnog izveštaja")
    
    response = requests.post(f"{IZVESTAJI_URL}/generiši", json={})
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    if response.status_code == 200:
        print("TEST PROŠAO - Izveštaj uspešno generisan")
        return response.json()
    else:
        print("TEST FAILED")
        return None


def test_2_generiši_filtrirani_izvestaj():
    """TEST 2: Generisanje izveštaja sa filterima"""
    print_separator("TEST 2: Generisanje sa filterima")
    
    parametri = {
        "transakcije_status": "uspesna",
        "transakcije_min_iznos": 1000,
        "transakcije_max_iznos": 100000,
        "penali_status": "kreiran",
        "penali_min_iznos": 5000,
        "dnevni_promet_days": 60,
        "uporedna_analiza_months": 6,
        "limit": 100
    }
    
    print(f"Parametri: {json.dumps(parametri, indent=2)}")
    
    response = requests.post(f"{IZVESTAJI_URL}/generiši", json=parametri)
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    if response.status_code == 200:
        print("TEST PROŠAO - Filtrirani izveštaj uspešno generisan")
        return response.json()
    else:
        print("TEST FAILED")
        return None


def test_3_quick_generate():
    """TEST 3: Brzo generisanje (GET metod)"""
    print_separator("TEST 3: Brzo generisanje (GET)")
    
    url = f"{IZVESTAJI_URL}/quick-generate?transakcije_status=uspesna&days=7&limit=20"
    print(f"URL: {url}")
    
    response = requests.get(url)
    
    print(f"Status: {response.status_code}")
    print(f"Content-Type: {response.headers.get('content-type')}")
    print(f"Content-Length: {len(response.content)} bytes")
    
    if response.status_code == 200 and response.headers.get('content-type') == 'application/pdf':
        # Sačuvaj PDF
        filename = f"test_quick_generate_{int(time.time())}.pdf"
        with open(filename, 'wb') as f:
            f.write(response.content)
        print(f"TEST PROŠAO - PDF sačuvan kao: {filename}")
        return True
    else:
        print("TEST FAILED")
        return False


def test_4_lista_izvestaja():
    """TEST 4: Lista svih izveštaja"""
    print_separator("TEST 4: Lista izveštaja")
    
    response = requests.get(f"{IZVESTAJI_URL}/lista")
    
    print(f"Status: {response.status_code}")
    
    if response.status_code == 200:
        data = response.json()
        print(f"Ukupno izveštaja: {data['ukupno']}")
        
        for izvestaj in data['izvestaji'][:5]:  # Prikaži prvih 5
            print(f"\n{izvestaj['file_name']}")
            print(f"Veličina: {izvestaj['size_mb']} MB")
            print(f"Kreiran: {izvestaj['created_at']}")
            print(f"URL: {izvestaj['download_url']}")
        
        print("TEST PROŠAO - Lista uspešno dohvaćena")
        return data
    else:
        print("TEST FAILED")
        return None


def test_5_preuzimanje_izvestaja(file_name):
    """TEST 5: Preuzimanje konkretnog izveštaja"""
    print_separator("TEST 5: Preuzimanje izveštaja")
    
    print(f"File: {file_name}")
    
    response = requests.get(f"{IZVESTAJI_URL}/preuzmi/{file_name}")
    
    print(f"Status: {response.status_code}")
    print(f"Content-Type: {response.headers.get('content-type')}")
    print(f"Content-Length: {len(response.content)} bytes")
    
    if response.status_code == 200 and response.headers.get('content-type') == 'application/pdf':
        # Sačuvaj PDF
        local_filename = f"downloaded_{file_name}"
        with open(local_filename, 'wb') as f:
            f.write(response.content)
        print(f"TEST PROŠAO - PDF preuzet i sačuvan kao: {local_filename}")
        return True
    else:
        print("TEST FAILED")
        return False


def test_6_brisanje_izvestaja(file_name):
    """TEST 6: Brisanje izveštaja"""
    print_separator("TEST 6: Brisanje izveštaja")
    
    print(f"Brišem: {file_name}")
    
    response = requests.delete(f"{IZVESTAJI_URL}/obrisi/{file_name}")
    
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    
    if response.status_code == 200:
        print("TEST PROŠAO - Izveštaj uspešno obrisan")
        return True
    else:
        print("TEST FAILED")
        return False


def test_7_validacija_parametara():
    """TEST 7: Validacija parametara"""
    print_separator("TEST 7: Validacija (nevalidni parametri)")
    
    # Test: Nevalidan limit
    parametri = {
        "limit": 1000  # Max je 500
    }
    
    response = requests.post(f"{IZVESTAJI_URL}/generiši", json=parametri)
    
    print(f"Status: {response.status_code}")
    
    if response.status_code == 422:  # Validation error
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        print("TEST PROŠAO - Validacija radi ispravno")
        return True
    else:
        print("TEST FAILED - Očekivan status 422")
        return False


def run_all_tests():
    """Pokreće sve testove"""
    print_separator("TESTIRANJE GENERATORA IZVEŠTAJA")
    print(f"Datum: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"API URL: {BASE_URL}")
    
    results = []
    
    # Test 1: Osnovni izveštaj
    result1 = test_1_generiši_osnovni_izvestaj()
    results.append(("Test 1 - Osnovni", result1 is not None))
    time.sleep(1)
    
    # Test 2: Filtrirani izveštaj
    result2 = test_2_generiši_filtrirani_izvestaj()
    results.append(("Test 2 - Filtrirani", result2 is not None))
    time.sleep(1)
    
    # Test 3: Brzo generisanje
    result3 = test_3_quick_generate()
    results.append(("Test 3 - Quick generate", result3))
    time.sleep(1)
    
    # Test 4: Lista
    result4 = test_4_lista_izvestaja()
    results.append(("Test 4 - Lista", result4 is not None))
    
    # Test 5: Preuzimanje (ako ima izveštaja)
    if result4 and result4['ukupno'] > 0:
        file_name = result4['izvestaji'][0]['file_name']
        result5 = test_5_preuzimanje_izvestaja(file_name)
        results.append(("Test 5 - Preuzimanje", result5))
    
    # Test 6: Brisanje (ako ima izveštaja za brisanje)
    if result1 and 'file_name' in result1:
        time.sleep(1)
        result6 = test_6_brisanje_izvestaja(result1['file_name'])
        results.append(("Test 6 - Brisanje", result6))
    
    # Test 7: Validacija
    time.sleep(1)
    result7 = test_7_validacija_parametara()
    results.append(("Test 7 - Validacija", result7))
    
    # Rezime
    print_separator("REZIME TESTOVA")
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "PASS" if result else "FAIL"
        print(f"{status} - {name}")
    
    print(f"\nUkupno: {passed}/{total} testova prošlo")
    
    if passed == total:
        print("\nSVI TESTOVI SU PROŠLI!")
    else:
        print(f"\n{total - passed} test(ova) nije prošlo")


if __name__ == "__main__":
    try:
        run_all_tests()
    except requests.exceptions.ConnectionError:
        print("\nGREŠKA: Nije moguće povezati se sa serverom!")
        print("Proverite da li je servis pokrenut na http://localhost:8001")
    except Exception as e:
        print(f"\nGREŠKA: {str(e)}")
