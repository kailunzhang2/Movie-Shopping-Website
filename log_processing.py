import sys

if __name__ == "__main__":
    TSs = []
    TJs = []
    with open(sys.argv[1]) as f:
        lines = f.readlines()
        for line in lines:
            temp = line.split(',')
            TSs.append(int(temp[0].split(':')[1]))
            TJs.append(int(temp[1].split(':')[1].strip("\n")))
    print("Avg TS in nano second: ", (sum(TSs)/len(TSs)), "Avg TJ in nano second: ", (sum(TJs)/len(TJs)))