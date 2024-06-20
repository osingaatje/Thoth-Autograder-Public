let fileSelector = undefined;
let jsonView = undefined;
let filterInputButton = undefined
let filterInputStrInput = undefined

let jsonObj = undefined;

document.addEventListener("DOMContentLoaded", function() {
    fileSelector = document.getElementById("file-selector");
    jsonView = document.getElementById("pretty-print-json");
    filterInputButton = document.getElementById("filter-input-btn")
    filterInputStrInput = document.getElementById("filter-input");
    
    fileSelector.addEventListener("dragover", (event) => {
        event.stopPropagation();
        event.preventDefault();
        // Style the drag-and-drop as a "copy file" operation.
        event.dataTransfer.dropEffect = "copy";
    });

    fileSelector.addEventListener("change", (event) => {
        const fileList = event.target.files;

        console.log("file???");
        if (fileList.length !== 1) {
            alert("Please input only one file!")
        }
        const file = fileList[0];
        const onFileRead = (e) => {
            const content = e.target.result;
            clearJsonView();

            jsonObj = JSON.parse(content);
            addToJsonView(jsonObj);
            console.log("JSON FROM FILE:", jsonObj);
        }

        console.log("fr");
        const fr = new FileReader();
        fr.onload = onFileRead;
        fr.readAsText(file);
    });


    filterInputButton.addEventListener("click", filterInput);
});

function clearJsonView() {
    jsonView.innerHTML = ''; // Clear previous content
}
function addToJsonView(jsonObject) {
    // const jsonObject = JSON.parse(line);
    const prettyJson = JSON.stringify(jsonObject, null, 2);
    const preElement = document.createElement('pre');
    preElement.innerHTML = syntaxHighlight(prettyJson);
    jsonView.appendChild(preElement);
}

function filterObj(obj, criteria){
    return obj.filter(function(o) {
        return Object.keys(criteria).every((c) => {
            return o[c] == criteria[c];
        });
    });

}

function filterInput() {
    const filt = filterInputStrInput.value
    const json = JSON.parse(filt);
    console.log("Filtering with: ", filt);
    const filteredObj = filterObj(jsonObj, json);
    clearJsonView();
    addToJsonView(filteredObj);

    console.log("RESULTS:", filteredObj);
}


function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}
