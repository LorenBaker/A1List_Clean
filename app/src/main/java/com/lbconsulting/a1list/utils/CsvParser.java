package com.lbconsulting.a1list.utils;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class CsvParser {

	/*
     * Copyright (c) 2014 Loren A Baker. All rights reserved.
	 * September 2014

	******************************************************************************************
	 *  The Comma Separated Values (CSV) File Format 
	 * 
	 * Each record is one line ...but 
	 * A record separator may consist of a line feed (ASCII/LF=0x0A),
	 * or a carriage return and line feed pair (ASCII/CRLF=0x0D 0x0A).
	 * 
	 * ...but: fields may contain embedded line-breaks (see below) so 
	 * a record may span more than one line. 
	 * 
	 * Fields are separated with commas.
	 * Example: John,Doe,120 any st.,"Anytown, WW",08123 
	 * 
	 * Leading and trailing space-characters adjacent to comma field 
	 * separators are ignored. So 
	 *   John  ,   Doe  ,... resolves to "John" and "Doe", etc. 
	 * Space characters can be spaces, or tabs. 
	 * 
	 * Fields with embedded commas must be delimited 
	 * with double-quote characters.
	 * In the above example. "Anytown, WW" had to be delimited 
	 * in double quotes because it had an embedded comma. 
	 * 
	 * Fields that contain double quote characters must be 
	 * surrounded by double-quotes, and the embedded 
	 * double-quotes must each be represented by a pair of 
	 * consecutive double quotes. So, 
	 * John "Da Man" Doe would convert to "John ""Da Man""",Doe, 120 any st.,... 
	 * 
	 * A field that contains embedded line-breaks must be surrounded 
	 * by double-quotes So:
	 *  Field 1: Conference room 1  
	 *  Field 2:
	 *     John,
	 *     Please bring the M. Mathers file for review  
	 *     -J.L.
	 *  Field 3: 10/18/2002
	 *    ... 
	 * would convert to: 
	 *    Conference room 1, "John,  
	 *    Please bring the M. Mathers file for review  
	 *    -J.L.
	 *    ",10/18/2002,... 
	 * 
	 * Note that this is a single CSV record, even though it takes up more 
	 * than one line in the CSV file. This works because the line breaks are 
	 * embedded inside the double quotes of the field. 
	 * 
	 * Implementation note: In Excel, leading spaces between the comma 
	 * used for a field separator and the double quote will sometimes cause 
	 * fields to be read in as unquoted fields, even though the first non-space 
	 * character is a double quote. To avoid this quirk, simply remove all leading 
	 * spaces after the field-separator comma and before the double quote 
	 * character in your CSV export files. 
	 * 
	 * Fields with leading or trailing spaces must be delimited with 
	 * double-quote characters.
	 * So to preserve the leading and trailing spaces around the last name 
	 * above: John ,"   Doe   ",... 
	 * 
	 * Fields may always be delimited with double quotes.
	 * The delimiters will always be discarded.   
	 */

    private static String mDelimiter = ",";
    private static String mQualifier = "\"";
    private static String mDoubleQualifier = mQualifier + mQualifier;
    private static String mCrLf = "\r\n";
    private static String mLineSeparator = System.getProperty("line.separator");
    private static String mCr = "\r";
    private static String mLf = "\n";
    private static String mTab = "\t";
    private static String mSpace = " ";

    private static String mCsvFileString = "";
    private static int mFilePointer = -1;

    private static final int SIMPLE_FIELD = 100;
    private static final int QUALIFIED_FIELD = 101;
    private static final String TEMP_CSV_STORAGE_FOLDER = "tempCsvStorageFolder";

    public static String toCSVString(ArrayList<String> fields) {

        ArrayList<String> csvFields = new ArrayList<>();

        for (String field : fields) {
            if (field.contains(mQualifier)) {
                // replace each Qualifier with a Double Qualifier
                // and then place a Qualifier string around the entire field
                field = field.replace(mQualifier, mDoubleQualifier);
                field = mQualifier + field + mQualifier;

            } else if (field.contains(mDelimiter) ||
                    field.contains(mCrLf) ||
                    field.contains(mLineSeparator) ||
                    field.contains(mLf) ||
                    field.startsWith(mSpace) ||
                    field.endsWith(mSpace) ||
                    field.startsWith(mTab) ||
                    field.endsWith(mTab)) {
                // then place a Qualifier string around the entire field
                field = mQualifier + field + mQualifier;
            }
            csvFields.add(field);
        }

        int count = 1;
        StringBuilder csvString = new StringBuilder();
        for (String field : csvFields) {
            if (csvFields.size() > count) {
                csvString.append(field).append(mDelimiter);
            } else {
                csvString.append(field);
            }
            count++;
        }

        return csvString.toString();
    }

 /*   public static String toCSVfileString(ArrayList<ArrayList<String>> records) {
        StringBuilder csvFileString = new StringBuilder();
        String csvRecordString = "";
        int count = 1;
        for (ArrayList<String> record : records) {
            csvRecordString = toCSVString(record);
            if (records.size() > count) {
                csvFileString.append(csvRecordString).append(mCrLf);
            } else {
                csvFileString.append(csvRecordString);
            }
        }
        return csvFileString.toString();
    }*/

  /*  public static Uri writeCsvFileToExternalStorage(Context context, ArrayList<ArrayList<String>> records,
                                                    String filename)
            throws IOException {

        Uri resultantUri = null;
        if (isExternalStorageWritable()) {

            String fileCsvString = toCSVfileString(records);
            if (filename == null) {
                // create unique filename
                filename = "f" + String.valueOf(System.currentTimeMillis());
            }
            if (strings.endsWithIgnoreCase(filename, ".csv")) {
                filename = filename.substring(0, filename.length() - 4);
            }

            File outputDir = new File(context.getExternalFilesDir(null), TEMP_CSV_STORAGE_FOLDER);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            if (outputDir.exists()) {
                File outputFile = File.createTempFile(filename, ".csv", outputDir);
                outputFile.createNewFile();
                FileWriter fw = new FileWriter(outputFile);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(fileCsvString);
                bw.close();
                fw.close();

                resultantUri = Uri.fromFile(outputFile);
            }
        }
        return resultantUri;
    }*/

    public static boolean deleteCsvFileFromExternalStorage(Uri csvFileUri) {
        boolean result = false;
        File file = new File(csvFileUri.getPath());
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static ArrayList<ArrayList<String>> CreateRecordAndFieldLists(String csvFileString) {
        mCsvFileString = csvFileString;

        // Establish variables to hold a field, a record, all records,
        // and the current character
        Character currentChar;
        ArrayList<Character> currentField = new ArrayList<Character>();
        String fieldString = "";
        ArrayList<String> currentRecord = new ArrayList<String>();
        ArrayList<ArrayList<String>> csvRecords = new ArrayList<ArrayList<String>>();

        // Initialize starting status mode and file pointer position.
        int status = SIMPLE_FIELD;
        mFilePointer = -1;
        try {
            while (!EOF()) {
                currentChar = ReadNextChar();
                if (currentChar.equals(mCr.charAt(0))) {
                    if (EOF()) {
                        break;
                    }
                    currentChar = ReadNextChar();
                    if (currentChar.equals(mLf.charAt(0))) {
                        // have a CRLF pair
                        switch (status) {
                            case SIMPLE_FIELD:
                                // reached the end of the record
                                fieldString = chars2String(currentField);
                                currentRecord.add(fieldString);
                                currentField = new ArrayList<Character>();
                                csvRecords.add(currentRecord);
                                currentRecord = new ArrayList<String>();
                                break;

                            case QUALIFIED_FIELD:
                                // have not reached the end of the field
                                // add the CRLF to the field
                                currentField.add(mCr.charAt(0));
                                currentField.add(mLf.charAt(0));
                                break;
                            default:
                                break;
                        }
                    }

                } else if (currentChar.equals(mLf.charAt(0))) {
                    // Encountered a LF that was not preceded with a CR!
                    switch (status) {
                        case SIMPLE_FIELD:
                            // Do nothing .... continue
                            break;

                        case QUALIFIED_FIELD:
                            // add the CRLF to the field
                            currentField.add(mCr.charAt(0));
                            currentField.add(mLf.charAt(0));
                            break;
                        default:
                            break;
                    }

                } else if (currentChar.equals(mDelimiter.charAt(0))) {
                    switch (status) {
                        case SIMPLE_FIELD:
                            // reached the end of the field
                            fieldString = chars2String(currentField);
                            currentRecord.add(fieldString);
                            currentField = new ArrayList<Character>();
                            break;

                        case QUALIFIED_FIELD:
                            // have not reached the end of the field
                            // add the delimiter to the field
                            currentField.add(mDelimiter.charAt(0));
                            break;
                        default:
                            break;
                    }

                } else if (currentChar.equals(mQualifier.charAt(0))) {
                    switch (status) {
                        case SIMPLE_FIELD:
                            // beginning of a QualifiedField
                            // ignore the quote mark
                            status = QUALIFIED_FIELD;
                            break;

                        case QUALIFIED_FIELD:
                            if (EOF()) {
                                break;
                            }
                            currentChar = ReadNextChar();
                            if (currentChar.equals(mQualifier.charAt(0))) {
                                // double quote marks
                                // include one of the quote marks in the field
                                currentField.add(mQualifier.charAt(0));

                            } else if (currentChar.equals(mDelimiter.charAt(0))) {
                                // Qualifier - Delimiter pair
                                // quote mark - comma pair
                                // reached the end of QualifiedField
                                fieldString = chars2String(currentField);
                                currentRecord.add(fieldString);
                                currentField = new ArrayList<Character>();
                                status = SIMPLE_FIELD;

                            } else if (currentChar.equals(mCr.charAt(0))) {
                                if (EOF()) {
                                    break;
                                }
                                currentChar = ReadNextChar();
                                if (!currentChar.equals(mLf.charAt(0))) {
                                    // Encountered a CR but not a CRLF pair!
                                    // Continuing as if a CRLF pair was found
                                }
                                // have a quote mark, CR,LF triplet
                                // reached the end of the record
                                fieldString = chars2String(currentField);
                                currentRecord.add(fieldString);
                                currentField = new ArrayList<Character>();
                                csvRecords.add(currentRecord);
                                currentRecord = new ArrayList<String>();
                                status = SIMPLE_FIELD;
                            } else {
                                // reached the end of QualifiedField
                                fieldString = chars2String(currentField);
                                currentRecord.add(fieldString);
                                currentField = new ArrayList<Character>();
                                status = SIMPLE_FIELD;

                                // ignore all characters until a comma or CRLF is found
                                while (true) {
                                    if (EOF()) {
                                        break;
                                    }
                                    currentChar = ReadNextChar();
                                    if (currentChar.equals(mDelimiter.charAt(0))) {
                                        // Qualifier - Delimiter pair
                                        // quote mark - comma pair
                                        // reached the end of QualifiedField
                                        break;
                                    }
                                    if (currentChar.equals(mCr.charAt(0))) {
                                        if (EOF()) {
                                            break;
                                        }
                                        currentChar = ReadNextChar();
                                        if (!currentChar.equals(mLf.charAt(0))) {
                                            // Encountered a CR but not a CRLF pair!
                                            // Continuing as if a CRLF pair was found
                                        }
                                        break;
                                    }
                                }
                            }

                            break;
                        default:
                            break;
                    }

                } else if (currentChar.equals(mSpace.charAt(0)) || currentChar.equals(mTab.charAt(0))) {
                    switch (status) {
                        case SIMPLE_FIELD:
                            // if you're at the start of a field,
                            // then ignore the space or tab
                            if (currentField.size() > 0) {
                                // field characters have been added
                                // so include the space or tab
                                currentField.add(currentChar);
                            }
                            break;

                        case QUALIFIED_FIELD:
                            // add the space or tab to all Qualified fields
                            currentField.add(currentChar);
                            break;
                        default:
                            break;
                    }
                } else {
                    currentField.add(currentChar);
                }

            }

            // Finished traversing the csv file
            // check that the current field and record have been added
            // to their lists

            if (currentField.size() > 0) {
                fieldString = chars2String(currentField);
                currentRecord.add(fieldString);
                currentField = new ArrayList<Character>();
                csvRecords.add(currentRecord);
                currentRecord = new ArrayList<String>();
            }

            if (currentRecord.size() > 0) {
                csvRecords.add(currentRecord);
            }

        } catch (Exception e) {
            if (Log.isLoggable("csvParser", Log.ERROR)) {
                Log.e("csvParser", "Exception error in CreateRecordAndFieldLists");
            }
            e.printStackTrace();
        }
        return csvRecords;
    }

    private static Character ReadNextChar() {
        Character nextChar = null;
        mFilePointer++;
        if (PointerInFileBounds()) {
            nextChar = mCsvFileString.charAt(mFilePointer);
        }
        return nextChar;
    }

    private static String chars2String(ArrayList<Character> field) {
        StringBuilder builder = new StringBuilder(field.size());
        for (Character ch : field) {
            builder.append(ch);
        }
        return builder.toString();
    }

    private static boolean EOF() {
        int nextPosition = mFilePointer + 1;
        return nextPosition >= mCsvFileString.length();
    }

    private static boolean PointerInFileBounds() {
        boolean result = false;
        if (mFilePointer > -1 && mFilePointer < mCsvFileString.length()) {
            result = true;
        }
        return result;
    }

    // Sample code:
	/*	protected void EmailCsvRecords() {
			FillCSVRecords();
			mCsvFileUri = null;
			try {
				mCsvFileUri = csvParser.writeCsvFileToExternalStorage(getActivity(), file, null);
			} catch (IOException e) {

				e.printStackTrace();
			}

			if (mCsvFileUri != null) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Test csv file");
				sendIntent.putExtra(Intent.EXTRA_STREAM, mCsvFileUri);
				sendIntent.setType("text/html");
				startActivityForResult(sendIntent, EMAIL_REQUEST_CODE);
			}

		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if (requestCode == EMAIL_REQUEST_CODE) {
				if (csvParser.deleteCsvFileFromExternalStorage(mCsvFileUri)) {
					Toast.makeText(getActivity(), "CSV file sucesfully deleted.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "CSV file NOT deleted.", Toast.LENGTH_SHORT).show();
				}

			}
		}
	*/
}
