"""
=================================================================
SCRIPT: Converter perguntas.txt → Firebase JSON (quizzes.json)
=================================================================

O QUE FAZ:
  Lê um arquivo .txt com perguntas no formato padronizado e gera
  um arquivo .json pronto para importar no Firebase Realtime Database.

COMO USAR:
  1. Edite o arquivo 'perguntas.txt' com suas perguntas
  2. Execute: python txt_para_firebase.py
  3. O arquivo 'quizzes.json' será gerado
  4. Vá ao Firebase Console → Realtime Database → ⋮ → Importar JSON
  5. Selecione o arquivo 'quizzes.json' e importe

FORMATO DO TXT (cada pergunta separada por linha em branco):
  Pergunta: Qual é a capital do Brasil?
  A: São Paulo
  B: Rio de Janeiro
  C: Brasília
  D: Salvador
  Resposta: C
  Categoria: Geografia

POR QUE ESSE SCRIPT?
  → O Firebase Realtime Database aceita importação de JSON diretamente
    pelo Console. Então, em vez de digitar pergunta por pergunta no
    Console (tedioso!), você edita um TXT simples e converte com 1 comando.

  → Você pode ter CENTENAS de perguntas no TXT, e o script gera o JSON
    certinho para todas. O app baixa do Firebase e sorteia N aleatórias.
=================================================================
"""

import json
import sys
import os


def parse_questions(txt_content: str) -> list:
    """
    Lê o conteúdo do TXT e retorna uma lista de dicionários,
    onde cada dicionário é uma pergunta no formato do Firebase.
    """
    questions = []

    # Separa o conteúdo por blocos (linhas em branco separam perguntas)
    # Remove comentários (linhas que começam com #)
    lines = [line for line in txt_content.strip().split('\n')
             if not line.strip().startswith('#')]
    content = '\n'.join(lines)

    blocks = content.strip().split('\n\n')

    # Mapeamento letra → índice (A=0, B=1, C=2, D=3)
    letter_to_index = {'A': 0, 'B': 1, 'C': 2, 'D': 3}

    for i, block in enumerate(blocks):
        block = block.strip()
        if not block:
            continue

        # Inicializa os campos
        question_data = {
            'questionText': '',
            'optionA': '',
            'optionB': '',
            'optionC': '',
            'optionD': '',
            'correctOptionIndex': 0,
            'category': 'Geral'
        }

        # Processa cada linha do bloco
        for line in block.split('\n'):
            line = line.strip()
            if not line:
                continue

            if line.startswith('Pergunta:'):
                question_data['questionText'] = line[len('Pergunta:'):].strip()
            elif line.startswith('A:'):
                question_data['optionA'] = line[len('A:'):].strip()
            elif line.startswith('B:'):
                question_data['optionB'] = line[len('B:'):].strip()
            elif line.startswith('C:'):
                question_data['optionC'] = line[len('C:'):].strip()
            elif line.startswith('D:'):
                question_data['optionD'] = line[len('D:'):].strip()
            elif line.startswith('Resposta:'):
                letter = line[len('Resposta:'):].strip().upper()
                question_data['correctOptionIndex'] = letter_to_index.get(letter, 0)
            elif line.startswith('Categoria:'):
                question_data['category'] = line[len('Categoria:'):].strip()

        # Valida que os campos essenciais estão preenchidos
        if question_data['questionText'] and question_data['optionA']:
            questions.append(question_data)
        else:
            print(f"⚠️  Bloco {i+1} ignorado (campos incompletos)")

    return questions


def main():
    # Caminhos dos arquivos
    script_dir = os.path.dirname(os.path.abspath(__file__))
    input_file = os.path.join(script_dir, 'perguntas.txt')
    output_file = os.path.join(script_dir, 'quizzes.json')

    # Verifica se o arquivo de entrada existe
    if not os.path.exists(input_file):
        print(f"❌ Arquivo '{input_file}' não encontrado!")
        print(f"   Crie o arquivo com suas perguntas e execute novamente.")
        sys.exit(1)

    # Lê o arquivo TXT
    with open(input_file, 'r', encoding='utf-8') as f:
        txt_content = f.read()

    # Converte para lista de perguntas
    questions = parse_questions(txt_content)

    if not questions:
        print("❌ Nenhuma pergunta válida encontrada no arquivo!")
        sys.exit(1)

    # Monta a estrutura JSON para o Firebase
    # A chave "quizzes" é o nó raiz no Realtime Database
    # Cada pergunta fica como um filho numerado (pergunta_1, pergunta_2...)
    firebase_json = {
        "quizzes": {}
    }

    for i, question in enumerate(questions):
        key = f"pergunta_{i + 1}"
        firebase_json["quizzes"][key] = question

    # Salva o JSON
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(firebase_json, f, ensure_ascii=False, indent=2)

    # Resumo
    print(f"✅ Conversão concluída!")
    print(f"   📄 Entrada:  {input_file}")
    print(f"   📦 Saída:    {output_file}")
    print(f"   📊 Total:    {len(questions)} perguntas")
    print()
    print(f"   Categorias encontradas:")
    categories = {}
    for q in questions:
        cat = q['category']
        categories[cat] = categories.get(cat, 0) + 1
    for cat, count in sorted(categories.items()):
        print(f"     • {cat}: {count} pergunta(s)")
    print()
    print(f"   📌 Próximo passo:")
    print(f"      1. Abra o Firebase Console → Realtime Database")
    print(f"      2. Clique em ⋮ (três pontos) → 'Importar JSON'")
    print(f"      3. Selecione o arquivo '{os.path.basename(output_file)}'")
    print(f"      4. Clique em 'Importar'")
    print(f"      5. Pronto! O app já vai baixar as perguntas automaticamente 🚀")


if __name__ == '__main__':
    main()
