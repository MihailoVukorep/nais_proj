"""
Test skripta za proveru rada mikroservisa

Testira sve glavne funkcionalnosti:
1. Health check
2. Kreiranje događaja
3. Sve tri složene analize
"""

import httpx
import time
from datetime import datetime

MIKROSERVIS_URL = "http://localhost:8001"


def test_health_check():
    """Test health check endpointa"""
    print("Testiranje health check endpointa...")
    try:
        response = httpx.get(f"{MIKROSERVIS_URL}/health", timeout=5.0)
        if response.status_code == 200:
            print("Health check: OK")
            return True
        else:
            print(f"Health check failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"Greška pri health check: {str(e)}")
        return False


def test_create_transakcija():
    """Test kreiranja transakcije"""
    print("\nTestiranje kreiranja transakcije...")
    try:
        data = {
            "faktura_id": 999,
            "iznos": 15000.50,
            "status": "uspesna",
            "potvrda": f"TEST-{int(time.time())}",
            "opis": "Test transakcija iz test skripte"
        }
        response = httpx.post(
            f"{MIKROSERVIS_URL}/api/dogadjaji/transakcija",
            json=data,
            timeout=5.0
        )
        if response.status_code == 201:
            print("Kreiranje transakcije: OK")
            print(f"   Response: {response.json()}")
            return True
        else:
            print(f"Kreiranje transakcije failed: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
    except Exception as e:
        print(f"Greška pri kreiranju transakcije: {str(e)}")
        return False


def test_create_penal():
    """Test kreiranja penala"""
    print("\nTestiranje kreiranja penala...")
    try:
        data = {
            "ugovor_id": 888,
            "iznos": 7500.00,
            "razlog": "Test penal - kašnjenje u isporuci",
            "status": "kreiran"
        }
        response = httpx.post(
            f"{MIKROSERVIS_URL}/api/dogadjaji/penal",
            json=data,
            timeout=5.0
        )
        if response.status_code == 201:
            print("Kreiranje penala: OK")
            print(f"   Response: {response.json()}")
            return True
        else:
            print(f"Kreiranje penala failed: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
    except Exception as e:
        print(f"Greška pri kreiranju penala: {str(e)}")
        return False


def test_dnevni_promet():
    """Test analize dnevnog prometa"""
    print("\nTestiranje analize dnevnog prometa...")
    try:
        response = httpx.get(
            f"{MIKROSERVIS_URL}/api/analize/dnevni-promet?days=7",
            timeout=10.0
        )
        if response.status_code == 200:
            data = response.json()
            print("Dnevni promet: OK")
            print(f"   Broj dana sa podacima: {len(data)}")
            if data:
                print(f"   Primer: {data[0]}")
            return True
        else:
            print(f"Dnevni promet failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"Greška pri dnevnom prometu: {str(e)}")
        return False


def test_rizicni_penali():
    """Test analize rizičnih penala"""
    print("\nTestiranje analize rizičnih penala...")
    try:
        response = httpx.get(
            f"{MIKROSERVIS_URL}/api/analize/rizicni-penali?min_iznos=5000&limit=5",
            timeout=10.0
        )
        if response.status_code == 200:
            data = response.json()
            print("Rizični penali: OK")
            print(f"Broj pronađenih penala: {len(data)}")
            if data:
                print(f"Primer: {data[0]}")
            return True
        else:
            print(f"Rizični penali failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"Greška pri rizičnim penalima: {str(e)}")
        return False


def test_uporedna_analiza():
    """Test uporedne analize performansi"""
    print("\nTestiranje uporedne analize performansi...")
    try:
        response = httpx.get(
            f"{MIKROSERVIS_URL}/api/analize/uporedna-analiza?months=1",
            timeout=10.0
        )
        if response.status_code == 200:
            data = response.json()
            print("Uporedna analiza: OK")
            print(f"   Broj nedelja sa podacima: {len(data)}")
            if data:
                print(f"   Primer: {data[0]}")
            return True
        else:
            print(f"Uporedna analiza failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"Greška pri uporednoj analizi: {str(e)}")
        return False


def main():
    """Glavna funkcija"""
    print("=" * 60)

    print(f"URL: {MIKROSERVIS_URL}")
    print(f"Vreme: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    print("=" * 60)
    
    results = []
    
    # Pokreni sve testove
    results.append(("Health Check", test_health_check()))
    
    # Sačekaj malo pre kreiranja podataka
    print("\nČekam 2 sekunde pre kreiranja test podataka...")
    time.sleep(2)
    
    results.append(("Kreiranje Transakcije", test_create_transakcija()))
    results.append(("Kreiranje Penala", test_create_penal()))
    
    # Sačekaj da se podaci upišu
    print("\nČekam 3 sekunde da se podaci upišu...")
    time.sleep(3)
    
    results.append(("Dnevni Promet", test_dnevni_promet()))
    results.append(("Rizični Penali", test_rizicni_penali()))
    results.append(("Uporedna Analiza", test_uporedna_analiza()))

    print("\n" + "=" * 60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "PASS" if result else "FAIL"
        print(f"{status}  {name}")
    
    print("=" * 60)
    print(f"Ukupno: {passed}/{total} testova prošlo")
    print("=" * 60)
    
    if passed == total:
        print("\nSvi testovi su prošli uspešno!")
        return 0
    else:
        print(f"\n{total - passed} test(ova) nije prošlo!")
        return 1


if __name__ == "__main__":
    exit(main())
