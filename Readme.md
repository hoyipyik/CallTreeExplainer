# CallTree Explainer

This is a code explainer focusing on explaining Java call tree via LLMs.

## Setup

### Neo4j

Install Docker and use docker compose to start
  ```bash
  docker compose up
  ```

Username is `neo4j`, password is `testpassword`

### Call Tree

  You may generate call tree using Jprofiler and export in XML format. The XML should be put
  inside `src/main/resources/callTree/`.

### Source Code

  Clone the repository under `src/main/resources/sourceCode`.

  ```bash
  git clone https://github.com/rsatrioadi/JHotDraw.git
  ```

### LLMs

#### Ollama (Recommand)

Ollama provides restful API support, which can be deployed via Docker. [See here](https://ollama.com/blog/ollama-is-now-available-as-an-official-docker-image)

Use the command below to run
```bash
docker run -d --gpus=all -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama;
docker exec -it ollama ollama pull llama3;
docker exec -it ollama ollama run llama3;
```

#### llama.cpp (Legacy)
  Here, we use `llama.cpp`to run LLama AI.

  Please clone within `src/main/` folder.

- Clone
  
  ```bash
  git clone https://github.com/ggerganov/llama.cpp.git
  cd llama.cpp
  ``` 
- Build and verify
  ```bash
  make # check that the build is running
  ./main -h # should print help manual for llama.cpp
  ```
- Download model weights

  You may choose the model you like.(Have tested 2 only)

  - Download `LLama-2` from [HuggingFace](https://huggingface.co/TheBloke?search_models=gguf&sort_models=downloads#models), for example we use `llama-2-7b-chat.Q2_K.gguf` by default.
  
  - Download `LLama-3`from [HuggingFace](https://huggingface.co/QuantFactory/Meta-Llama-3-8B-GGUF/tree/main), here we use `Meta-Llama-3-8B.Q2_K.gguf`by default.

 
  