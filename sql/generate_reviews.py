import argparse
import random
import datetime
import re
from pathlib import Path


def count_restaurants(insert_data_path: Path) -> int:
    txt = insert_data_path.read_text(encoding='utf-8')
    # Conta quante occorrenze di INSERT INTO RistorantiTheKnife esistono
    return len(re.findall(r"INSERT\s+INTO\s+RistorantiTheKnife\b", txt, flags=re.IGNORECASE))


REVIEWS_5_STARS = [
    'Exquisite food! I\'ll definitely come back.',
    'Exceptional in every way. Highly recommended.',
    'Beautiful presentation and extraordinary flavors.',
    'Memorable experience, will return for special occasions.',
    'Charming atmosphere and impeccable service.',
    'A delight for the palate, everything was perfect!'
]

REVIEWS_4_STARS = [
    'Excellent food, good service overall.',
    'Interesting dishes, some minor imprecisions.',
    'Pleasant experience overall.',
    'Good value for money, recommended.',
    'Good cuisine, pleasant ambiance.'
]

REVIEWS_3_STARS = [
    'Acceptable but nothing extraordinary.',
    'Solid dishes, nothing special.',
    'Normal experience, adequate service.',
    'Expensive but acceptable for the quality.',
    'Overall pleasant, might return.'
]

REVIEWS_2_STARS = [
    'Slow service, mediocre food.',
    'Not what I expected.',
    'Bland dishes, disappointing experience.',
    'High prices for the quality offered.',
    'Long wait time, poor execution.'
]

REVIEWS_1_STAR = [
    'Poor service and cold dishes, total disappointment.',
    'I don\'t recommend it at all, horrible experience.',
    'Terrible, will never return.',
    'Worst restaurant I\'ve ever visited.',
    'Low quality, rude staff.'
]


def choose_text_for_rating(rating: int, rnd: random.Random) -> str:
    """Seleziona recensione in base alla valutazione."""
    if rating == 5:
        return rnd.choice(REVIEWS_5_STARS)
    elif rating == 4:
        return rnd.choice(REVIEWS_4_STARS)
    elif rating == 3:
        return rnd.choice(REVIEWS_3_STARS)
    elif rating == 2:
        return rnd.choice(REVIEWS_2_STARS)
    else:  # rating == 1
        return rnd.choice(REVIEWS_1_STAR)


def make_timestamp(rnd: random.Random) -> str:
    """Genera un timestamp casuale nei ultimi 180 giorni."""
    days = rnd.randint(1, 180)
    dt = datetime.datetime.now() - datetime.timedelta(days=days)
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def main():
    p = argparse.ArgumentParser(
        description='Genera recensioni per i ristoranti.'
    )
    p.add_argument('--offset', type=int, default=0,
                   help='Offset ID aggiunto agli ID base dei ristoranti (default: 0 per un DB nuovo)')
    p.add_argument('--min', type=int, default=3,
                   help='Minimo di recensioni per ristorante (default: 3)')
    p.add_argument('--max', type=int, default=5,
                   help='Massimo di recensioni per ristorante (default: 5)')
    p.add_argument('--out', type=Path, default=Path('sql/insert_reviews.sql'),
                   help='File SQL di output (default: sql/insert_reviews.sql)')
    p.add_argument('--data', type=Path, default=Path('sql/insert_data.sql'),
                   help='Percorso a insert_data.sql per contare i ristoranti')
    args = p.parse_args()

    if not args.data.exists():
        raise SystemExit(f"insert_data.sql non trovato in {args.data}. Esegui prima csv_to_sql.py.")

    n_rest = count_restaurants(args.data)
    rnd = random.Random(42)  # deterministico

    # 5 utenti: gli utenti mock creati (ID 1-5)
    users = [1, 2, 3, 4, 5]

    out_lines = []
    out_lines.append('-- Generato da generate_reviews.py')
    out_lines.append(f'-- Ristoranti trovati: {n_rest}')
    out_lines.append(f'-- Recensioni per ristorante: {args.min}-{args.max} (casuale)')
    out_lines.append(f'-- Offset: {args.offset}')
    out_lines.append('')

    # Crea insert raggruppate su più righe, svuotando ogni N righe
    batch = []
    total_reviews = 0

    for base_id in range(1, n_rest + 1):
        restaurant_id = base_id + args.offset
        # RNG deterministico per ristorante per riproducibilità
        r_rnd = random.Random(100000 + base_id)

        # Numero casuale di recensioni: 3-5 per ristorante
        num_reviews = r_rnd.randint(args.min, args.max)

        # Seleziona valutazioni casuali per questo ristorante (garantisce varietà)
        ratings = [r_rnd.randint(1, 5) for _ in range(num_reviews)]

        # 3-5 recensioni per ristorante, quindi 5 utenti distinti sono sufficienti per evitare conflitti UNIQUE.
        review_users = r_rnd.sample(users, num_reviews)

        for rating, uid in zip(ratings, review_users):
            text = choose_text_for_rating(rating, r_rnd)
            text_sql = text.replace("'", "''")
            ts = make_timestamp(r_rnd)
            batch.append(f"({restaurant_id}, {uid}, {rating}, '{text_sql}', '{ts}')")
            total_reviews += 1

        # Svuota il batch ogni 500 righe per mantenere le istruzioni gestibili
        if len(batch) >= 500:
            out_lines.append('INSERT INTO Recensioni (id_ristorante, id_utente, stelle, testo, data_inserimento) VALUES')
            out_lines.append(',\n'.join(batch))
            out_lines.append('ON CONFLICT (id_ristorante, id_utente) DO NOTHING;')
            out_lines.append('')
            batch = []

    # Svuota il batch rimanente
    if batch:
        out_lines.append('INSERT INTO Recensioni (id_ristorante, id_utente, stelle, testo, data_inserimento) VALUES')
        out_lines.append(',\n'.join(batch))
        out_lines.append('ON CONFLICT (id_ristorante, id_utente) DO NOTHING;')
        out_lines.append('')

    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text('\n'.join(out_lines), encoding='utf-8')

    print(f'Generati {args.out}')
    print(f'  - Ristoranti: {n_rest}')
    print(f'  - Recensioni: {total_reviews} (~{total_reviews // n_rest} ')
    print(f'  - Recensioni per ristorante: {args.min}-{args.max} ')


if __name__ == '__main__':
    main()
