
from flask import Flask, request, jsonify
import json
import random
import os
import math

# --- Configurazione ---
Q_TABLE_FILE = "q_table.json"
# Hyperparameters
GAMMA = 0.9
EPSILON = 0.1
# Alpha (learning rate) dinamico
ALPHA_START = 0.5
ALPHA_MIN = 0.01
ALPHA_DECAY_RATE = 0.9999

app = Flask(__name__)

# --- Variabili Globali ---
q_table = {}
agent_history_for_current_game = []
total_games_played = 0
total_reward = 0.0

# --- Logica di Normalizzazione dello Stato ---
def get_symmetries(board):
    symmetries = []
    current_board = board
    for _ in range(4):
        symmetries.append(current_board)
        symmetries.append([list(row) for row in zip(*current_board)])
        current_board = [list(row) for row in zip(*current_board[::-1])]
    return symmetries

def get_canonical_state_key(board):
    symmetries = get_symmetries(board)
    canonical_form = min([tuple(map(tuple, b)) for b in symmetries])
    return "".join("".join(row) for row in canonical_form)

# --------------------------------------------------------

def get_available_actions(board):
    actions = []
    for r, row in enumerate(board):
        for c, cell in enumerate(row):
            if cell == "":
                actions.append((r, c))
    return actions

def load_q_table():
    if os.path.exists(Q_TABLE_FILE):
        with open(Q_TABLE_FILE, 'r') as f:
            print("Caricamento Q-table esistente...")
            return json.load(f)
    print("Nessuna Q-table trovata, ne verrà creata una nuova.")
    return {}

def save_q_table(q_table_to_save):
    with open(Q_TABLE_FILE, 'w') as f:
        json.dump(q_table_to_save, f)
    print("Q-table salvata.")

def action_to_key(action):
    return f"{action[0]},{action[1]}"

def choose_action(board):
    available_actions = get_available_actions(board)
    if not available_actions:
        return None

    if random.uniform(0, 1) < EPSILON:
        return random.choice(available_actions)
    else:
        state_key = get_canonical_state_key(board)
        state_q_values = q_table.get(state_key, {})

        if not state_q_values:
            return random.choice(available_actions)

        max_q = -float('inf')
        best_action = None
        random.shuffle(available_actions)
        for r, c in available_actions:
            q_val = state_q_values.get(action_to_key((r, c)), 0.0)
            if q_val > max_q:
                max_q = q_val
                best_action = (r, c)
        
        return best_action if best_action else random.choice(available_actions)

def check_game_over(board):
    lines = board + list(zip(*board)) + [[board[i][i] for i in range(3)], [board[i][2-i] for i in range(3)]]
    for line in lines:
        if line.count('X') == 3: return 'X'
        if line.count('O') == 3: return 'O'
    if not get_available_actions(board): return 'draw'
    return None

def get_current_alpha():
    """Calcola l'alpha corrente basato sul numero di partite giocate."""
    # Decadimento esponenziale: alpha = start * (decay_rate ^ N_games)
    return max(ALPHA_MIN, ALPHA_START * (ALPHA_DECAY_RATE ** total_games_played))

def update_q_table_from_history(history, final_reward):
    global q_table
    alpha = get_current_alpha()
    reward = final_reward
    for step in reversed(history):
        state_key = get_canonical_state_key(step['board'])
        action = step['action']
        
        if state_key not in q_table: q_table[state_key] = {}
        old_q = q_table[state_key].get(action, 0.0)
        
        q_table[state_key][action] = old_q + alpha * (reward - old_q)
        reward *= GAMMA
    print(f"Q-Table aggiornata con alpha={alpha:.4f}. Ricompensa finale: {final_reward}")

def record_game_and_print_stats(reward):
    global total_games_played, total_reward
    total_games_played += 1
    total_reward += reward
    average_reward = total_reward / total_games_played if total_games_played > 0 else 0
    print(f"--- Partita N.{total_games_played} terminata. Ricompensa: {reward:.1f}. Ricompensa media: {average_reward:.3f} ---")

@app.route('/move', methods=['POST'])
def get_move():
    global agent_history_for_current_game
    data = request.get_json()
    board = data['board']
    
    action = choose_action(board)
    
    if not action:
        return jsonify({'error': 'No available moves'}), 400
    
    agent_history_for_current_game.append({'board': board, 'action': action_to_key(action)})
    
    temp_board = [row[:] for row in board]
    temp_board[action[0]][action[1]] = 'O'
    
    winner = check_game_over(temp_board)
    
    if winner == 'O':
        update_q_table_from_history(agent_history_for_current_game, 1.0)
        save_q_table(q_table)
        record_game_and_print_stats(1.0)
        agent_history_for_current_game.clear()
    elif winner == 'draw':
        update_q_table_from_history(agent_history_for_current_game, 0.0)
        save_q_table(q_table)
        record_game_and_print_stats(0.0)
        agent_history_for_current_game.clear()

    print(f"Board ricevuta. Mossa dell'agente: {action}")
    return jsonify({'move': [action[0], action[1]]})

@app.route('/turn', methods=['POST'])
def post_turn():
    global agent_history_for_current_game
    data = request.get_json()
    board = data['board']
    winner = check_game_over(board)
    if winner == 'X': # L'utente ha vinto
        print("L'utente ha vinto. Apprendo dalla sconfitta...")
        update_q_table_from_history(agent_history_for_current_game, -1.0)
        save_q_table(q_table)
        record_game_and_print_stats(-1.0)
        agent_history_for_current_game.clear()
    return jsonify({'status': 'ok'})

@app.route('/reset', methods=['POST'])
def reset_game():
    global agent_history_for_current_game
    agent_history_for_current_game.clear()
    print("Partita resettata. Storia dell'agente cancellata.")
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    q_table = load_q_table()
    print("Avvio del server Flask per Tic-Tac-Toe...")
    app.run(host='0.0.0.0', port=8080)
