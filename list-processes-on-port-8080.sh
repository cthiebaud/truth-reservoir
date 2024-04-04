
#!/bin/bash
# The above line is the shebang line, which specifies the interpreter (in this case, Bash).

# lsof command lists open files and associated processes
# -i flag is used to specify internet addresses
# :8080 specifies the port number to filter on
lsof -i :8080
