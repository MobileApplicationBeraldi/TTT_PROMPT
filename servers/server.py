from flask import Flask, request, jsonify
import json
import random
import os

# --- Configurazione ---
Q_TABLE_FILE = "q_table.json"
ALPHA = 0.1
GAMMA = 0.9
EPSILON = 0.1

app = Flask(__name__)

q_table = {}
agent_history_for_current_game = []

def get_state_key(board):
    return "".join("".join(str(c) for c in row) for row in board)

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
        state_key = get_state_key(board)
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

def update_q_table_from_history(history, final_reward):
    global q_table
    reward = final_reward
    for step in reversed(history):
        state = step['state']
        action = step['action']
        
        if state not in q_table: q_table[state] = {}
        old_q = q_table[state].get(action, 0.0)
        
        q_table[state][action] = old_q + ALPHA * (reward - old_q)
        reward *= GAMMA
    print(f"Q-Table aggiornata. Ricompensa finale applicata: {final_reward}")

@app.route('/move', methods=['POST'])
def get_move():
    global agent_history_for_current_game
    data = request.get_json()
    board = data['board']
    
    state_key = get_state_key(board)
    action = choose_action(board)
    
    if not action:
        return jsonify({'error': 'No available moves'}), 400
    
    agent_history_for_current_game.append({'state': state_key, 'action': action_to_key(action)})
    
    temp_board = [row[:] for row in board]
    temp_board[action[0]][action[1]] = 'O'
    
    winner = check_game_over(temp_board)
    
    if winner == 'O':
        update_q_table_from_history(agent_history_for_current_game, 1.0)
        save_q_table(q_table)
        agent_history_for_current_game.clear()
    elif winner == 'draw':
        update_q_table_from_history(agent_history_for_current_game, 0.5)
        save_q_table(q_table)
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
        update_q_table_from_history(agent_history_for_current_game, -1.0) # Punizione
        save_q_table(q_table)
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
